plugins {
    alias(libs.plugins.kotlin.serialization)
    id("hnau.android.lib")
}

kotlin {
    linuxX64()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
        }
    }
}
