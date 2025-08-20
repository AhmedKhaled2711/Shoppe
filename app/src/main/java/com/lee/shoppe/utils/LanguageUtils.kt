package com.lee.shoppe.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

object LanguageUtils {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val SELECTED_COUNTRY = "Locale.Helper.Selected.Country"

    fun setLocale(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            "ar" -> Locale("ar")
            else -> Locale("en")
        }
        persistLanguage(context, locale)
        updateResources(context, locale)
    }

    private fun persistLanguage(context: Context, locale: Locale) {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        preferences.edit()
            .putString(SELECTED_LANGUAGE, locale.language)
            .putString(SELECTED_COUNTRY, locale.country)
            .apply()
    }

    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    @SuppressLint("ObsoleteSdkInt")
    fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For API 24+ use setLocale
            configuration.setLocale(locale)
            
            // For API 21+ use setLocales
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            }
            
            // Create new configuration context and update the resources
            val contextWrapper = context.createConfigurationContext(configuration)
            resources.updateConfiguration(
                configuration,
                resources.displayMetrics
            )
            
            // Update the application context
            contextWrapper.resources.updateConfiguration(
                configuration,
                resources.displayMetrics
            )
        } else {
            // For older versions
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    fun applyLanguage(context: Context) {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val language = preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
        val country = preferences.getString(SELECTED_COUNTRY, "") ?: ""
        
        val locale = if (country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }
        
        updateResources(context, locale)
    }
}
