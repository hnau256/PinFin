plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.app")
}

android {
    namespace = "hnau.pinfin.client"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFile("proguard-rules.pro")
        }
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.android.activity.compose)
            implementation(libs.android.appcompat)
            implementation(libs.android.datastore)
            implementation(libs.slf4j.simple)
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:data"))
            implementation(project(":pinfin:client:app"))
            implementation(project(":pinfin:client:compose"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:client:projector"))
        }
    }
}