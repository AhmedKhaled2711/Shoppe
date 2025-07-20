package com.lee.shoppe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShoppeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Places API initialization removed
    }
} 