plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("java-gradle-plugin")
}

dependencies {
    implementation(libs.gradle.plugin.kotlin.jvm)
    implementation(libs.gradle.plugin.android)
    implementation(libs.gradle.plugin.compose)
    implementation(libs.kotest.framework.multiplatform.plugin)
    implementation(libs.arrow.core)
    implementation(libs.kotlin.serialization.json)
}

val javaVersion = JavaVersion.valueOf(libs.versions.java.get())
val javaVersionNumber = javaVersion.ordinal + 1

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersionNumber))
}

kotlin {
    jvmToolchain(javaVersionNumber)
}

gradlePlugin {
    plugins.create("Kotlin multiplatform library") {
        id = "hnau.kotlin.multiplatform"
        implementationClass = "hnau.plugin.HnauKotlinMultiplatformLibPlugin"
    }
    plugins.create("Android application") {
        id = "hnau.android.app"
        implementationClass = "hnau.plugin.HnauAndroidAppPlugin"
    }
    plugins.create("Dependencies") {
        id = "hnau.fixdependencies"
        implementationClass = "hnau.plugin.fixdependencies.HnauFixDependenciesPlugin"
    }
}
