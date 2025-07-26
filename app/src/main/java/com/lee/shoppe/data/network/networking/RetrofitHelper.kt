package com.lee.shoppe.data.network.networking

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    private const val baseURL = "https://mad44-alex-android-team1.myshopify.com/admin/api/2024-04/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val credentials = Credentials.basic("24fd0ff00945a069ab50bb3d6f8bf329", "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")
        val request = chain.request().newBuilder()
            .addHeader("Authorization", credentials)
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    val retrofitInstance: Retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseURL)
        .build()
}

object RetrofitHelperPayment {
    private const val BASE_URL = "https://api.stripe.com/"
    private const val API_KEY = "sk_test_51PSbTgDoeYNScbTmfJjKgahaCBVsau7NFPOIrpy3hphWLFv3NoSONjRMcBNkilYz0GVFUkxW6XJyHbh0VHBZbf2y00iBVrATWB"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    val retrofitInstance: Retrofit =
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
}