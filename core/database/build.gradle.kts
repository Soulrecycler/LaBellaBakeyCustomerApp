plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            api(libs.androidx.datastore.preferences)
            implementation(libs.okio)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.bakery.customer.core.database"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}
