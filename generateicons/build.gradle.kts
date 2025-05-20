plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.hnau.kotlin)
    implementation(libs.hnau.gen.kt)
}

application {
    mainClass = "hnau.generateicons.GenerateIconsKt"
}