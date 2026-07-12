plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.database)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "com.bakery.customer.feature.auth"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}
