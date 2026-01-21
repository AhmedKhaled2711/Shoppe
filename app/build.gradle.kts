import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.lee.shoppe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lee.shoppe"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add BuildConfig fields - Read from local.properties for security
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        
        buildConfigField("String", "SHOPIFY_API_KEY", "\"${localProperties.getProperty("SHOPIFY_API_KEY", "")}\"")
        buildConfigField("String", "SHOPIFY_PASSWORD", "\"${localProperties.getProperty("SHOPIFY_PASSWORD", "")}\"")
        buildConfigField("String", "STRIPE_API_KEY", "\"${localProperties.getProperty("STRIPE_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.1"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.play.services.auth)
    implementation(libs.lottie.compose)

    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation("com.google.accompanist:accompanist-navigation-animation:0.34.0")

    implementation(libs.coil.compose)

    //Map
    implementation("com.google.maps.android:maps-compose:4.0.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //Bottom Navigation
    implementation ("com.github.0xRahad:RioBottomNavigation:1.0.2")

    //Animation
    implementation(libs.androidx.animation)


}
