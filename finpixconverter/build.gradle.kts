plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.ooxml)
    implementation(libs.hnau.kotlin)
    implementation(project(":pinfin:data"))
}

application {
    mainClass = "hnau.finpixconverter.FinPixConverterKt"
}