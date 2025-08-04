package com.lee.shoppe.data.network.networking

import android.content.Context
import com.lee.shoppe.BuildConfig
import com.lee.shoppe.util.NetworkUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val baseURL = "https://mad44-alex-android-team1.myshopify.com/admin/api/2024-04/"
    private val CACHE_DIRECTORY = "http-cache"
    private val MAX_RETRIES = 3
    private val RETRY_DELAY_MS = 1000L
    private val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
    private val MAX_AGE = 60 * 60 // 1 hour
    private val STALE_WHILE_REVALIDATE = 60 * 60 * 24 // 1 day

    private var cache: Cache? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val credentials = Credentials.basic(
            BuildConfig.SHOPIFY_API_KEY,
            BuildConfig.SHOPIFY_PASSWORD
        )
        val originalRequest = chain.request()
        
        // Don't add cache headers for DELETE requests or if it's a force refresh
        val cacheControl = when {
            originalRequest.method.equals("DELETE", ignoreCase = true) ||
            originalRequest.header("Cache-Control")?.contains("no-cache") == true -> {
                "no-cache, no-store, must-revalidate"
            }
            else -> "public, max-age=$MAX_AGE, max-stale=$STALE_WHILE_REVALIDATE"
        }
        
        val request = originalRequest.newBuilder()
            .addHeader("Authorization", credentials)
            .addHeader("Cache-Control", cacheControl)
            .removeHeader("Pragma") // Remove any existing Pragma header
            .build()
            
        chain.proceed(request)
    }

    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        
        // Skip retry for non-GET requests or if it's a force refresh
        if (!request.method.equals("GET", ignoreCase = true) || 
            request.header("Cache-Control")?.contains("no-cache") == true) {
            return@Interceptor chain.proceed(request)
        }
        
        var lastException: Exception? = null
        var response: Response? = null

        for (attempt in 0..MAX_RETRIES) {
            if (attempt > 0) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted during retry", e)
                }
            }

            try {
                response?.close() // Close previous response if any
                response = chain.proceed(request)
                
                // If response is successful, return it immediately
                if (response.isSuccessful) {
                    return@Interceptor response
                }
                
                // If this is the last attempt, return the error response
                if (attempt == MAX_RETRIES) {
                    return@Interceptor response
                }
                
                // Close the response body for unsuccessful responses (except the last attempt)
                response.body?.close()
                
            } catch (e: Exception) {
                lastException = e
                // If this is the last attempt or not a retryable exception, rethrow
                if (attempt == MAX_RETRIES || 
                    (e !is java.net.SocketTimeoutException && 
                     e !is java.net.ConnectException && 
                     e !is java.net.UnknownHostException)) {
                    throw e
                }
            }
        }

        // This line should never be reached due to the logic above
        throw lastException ?: IOException("Failed to get response after $MAX_RETRIES attempts")
    }

    @Synchronized
    private fun getCache(context: Context): Cache {
        return cache ?: synchronized(this) {
            val cacheDir = File(context.cacheDir, CACHE_DIRECTORY).apply {
                if (!exists()) mkdirs()
            }
            Cache(cacheDir, CACHE_SIZE).also { cache = it }
        }
    }

    fun createClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Add interceptors in the correct order
            addInterceptor(loggingInterceptor)
            addInterceptor(authInterceptor)
            addInterceptor(provideOfflineCacheInterceptor(context))
            addNetworkInterceptor(provideCacheInterceptor())
            addNetworkInterceptor(retryInterceptor)
            
            // Configure timeouts and cache
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            cache(getCache(context))
        }.build()
    }

    private fun provideCacheInterceptor(): Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        
        // Only modify cache headers if this is a GET request and not already set
        if (chain.request().method.equals("GET", ignoreCase = true) && 
            chain.request().header("Cache-Control") == null) {
            val cacheControl = CacheControl.Builder()
                .maxAge(MAX_AGE, TimeUnit.SECONDS)
                .maxStale(STALE_WHILE_REVALIDATE, TimeUnit.SECONDS)
                .build()
                
            return@Interceptor response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
        
        response
    }

    private fun provideOfflineCacheInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            if (!NetworkUtil.isNetworkAvailable(context)) {
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }
    }

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .client(createClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseURL)
            .build()
    }
    
    companion object {
        // Removed duplicate constants that were moved to class properties
        private const val CACHE_DIRECTORY = "http-cache"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }
}

@Singleton
class RetrofitHelperPayment @Inject constructor() {
    private val BASE_URL = "https://api.stripe.com/"
    private val API_KEY = BuildConfig.STRIPE_API_KEY
    private val STRIPE_VERSION = "2023-10-16"
    
    companion object {
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Stripe-Version", STRIPE_VERSION)
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }
}