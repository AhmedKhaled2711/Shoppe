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
        
        // Add BuildConfig fields
        buildConfigField("String", "SHOPIFY_API_KEY", "\"24fd0ff00945a069ab50bb3d6f8bf329\"")
        buildConfigField("String", "SHOPIFY_PASSWORD", "\"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\"")
        buildConfigField("String", "STRIPE_API_KEY", "\"sk_test_51PSbTgDoeYNScbTmfJjKgahaCBVsau7NFPOIrpy3hphWLFv3NoSONjRMcBNkilYz0GVFUkxW6XJyHbh0VHBZbf2y00iBVrATWB\"")
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

    implementation(libs.coil.compose)

    //Map
    implementation("com.google.maps.android:maps-compose:4.0.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

}
