plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.lib")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.hnau.app)
            implementation(libs.hnau.compose)
            implementation(libs.android.datastore)
        }
    }
}