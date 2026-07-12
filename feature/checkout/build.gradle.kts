plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.network)
            implementation(projects.feature.cart)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}

android {
    namespace = "com.bakery.customer.feature.checkout"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}
