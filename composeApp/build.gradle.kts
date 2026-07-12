plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.designsystem)
            implementation(projects.core.ui)
            implementation(projects.feature.auth)
            implementation(projects.feature.home)
            implementation(projects.feature.catalog)
            implementation(projects.feature.search)
            implementation(projects.feature.cart)
            implementation(projects.feature.checkout)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)
            implementation(compose.uiTooling)
        }
    }
}

android {
    namespace = "com.bakery.customer.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.bakery.customer.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
