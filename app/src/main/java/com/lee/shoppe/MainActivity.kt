package com.lee.shoppe

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.lee.shoppe.ui.navigation.ECommerceNavHost
import com.lee.shoppe.ui.theme.ShoppeTheme
import com.lee.shoppe.ui.viewmodel.MainViewModel
import com.lee.shoppe.utils.LanguageUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash screen while loading
        val splashScreen = installSplashScreen()

        // Apply saved language before setting content
        LanguageUtils.applyLanguage(this)

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            val configuration = LocalConfiguration.current
            val context = LocalContext.current
            
            // Track language changes
            var currentLocale by remember { mutableStateOf(Locale.getDefault()) }
            
            // Update UI when configuration changes
            LaunchedEffect(configuration) {
                currentLocale = configuration.locales[0] ?: Locale.getDefault()
            }
            
            // Apply the selected language
            DisposableEffect(Unit) {
                val config = Configuration(configuration)
                config.setLocale(Locale(LanguageUtils.getLanguage(context)))
                context.createConfigurationContext(config)
                
                onDispose {}
            }
            
            ShoppeTheme {
                val navController = rememberNavController()
                ECommerceNavHost(navController = navController)
            }
        }
    }
}