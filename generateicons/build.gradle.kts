plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
    implementation(project(":common:kotlin"))
    implementation(project(":common:ktgen"))
}

application {
    mainClass = "hnau.generateicons.GenerateIconsKt"
}