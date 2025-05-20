plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hnau.app)
            implementation(project(":pinfin:data"))
            implementation(libs.kotlin.datetime)
            implementation(libs.ktor.network)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.kotlin.serialization.cbor)
        }
    }
}
