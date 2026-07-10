plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidxRoom) apply false
}

tasks.register("installGitHooks") {
    group = "setup"
    description = "Points Git at the committed .githooks directory (Conventional Commits check)."
    doLast {
        exec { commandLine("git", "config", "core.hooksPath", ".githooks") }
    }
}
