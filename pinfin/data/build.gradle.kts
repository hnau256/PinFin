plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.kmp")
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.kotlin)
                implementation(libs.kotlin.datetime)
                implementation(libs.enumvalues.annotations)
                implementation(libs.bignum)
            }
        }

        androidMain {
            kotlin.srcDir("build/generated/ksp/metadata/androidMain/kotlin")
        }
    }
}
