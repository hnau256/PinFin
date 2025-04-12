plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.lib")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":common:compose"))
            implementation(libs.android.datastore)
        }
    }
}