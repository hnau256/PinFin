plugins {
    alias(libs.plugins.kotlin.serialization)
    id("hnau.android.lib")
}

kotlin {
    linuxX64()
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
        }
    }
}
