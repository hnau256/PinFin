plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.model)
                implementation(project(":pinfin:data"))
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.io)
                implementation(libs.ktor.network)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.serialization.cbor)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
                implementation(libs.enumvalues.annotations)
                implementation(libs.bignum)
            }
        }
    }
}
