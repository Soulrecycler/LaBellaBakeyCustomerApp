enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "bakery-customer"

include(":composeApp")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:designsystem")
include(":core:ui")
include(":feature:auth")
include(":feature:home")
include(":feature:search")
include(":feature:catalog")
include(":feature:cart")
include(":feature:checkout")
