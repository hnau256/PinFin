plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.model)
                implementation(project(":pinfin:data"))
                implementation(libs.kotlin.io)
                implementation(libs.ktor.network)
                implementation(libs.bignum)
            }
        }
    }
}
