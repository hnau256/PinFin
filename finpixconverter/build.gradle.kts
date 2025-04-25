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
    implementation(project(":common:kotlin"))
    implementation(project(":pinfin:repository"))
}

application {
    mainClass = "hnau.finpixconverter.FinPixConverterKt"
}