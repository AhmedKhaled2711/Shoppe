// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    kotlin("android") version "2.0.0" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
}