plugins {
    alias(libs.plugins.kotlin.serialization)
    id("hnau.android.lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
        }
    }
}
