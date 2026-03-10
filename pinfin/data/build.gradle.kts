plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bignum)
            }
        }
    }
}
