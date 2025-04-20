plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.app")
}

android {
    namespace = "hnau.pinfin"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            isShrinkResources = false
            isMinifyEnabled = false
            isDebuggable = false
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
            implementation(project(":common:android"))
            implementation(project(":common:compose"))
            implementation(project(":pinfin:client:data"))
            implementation(project(":pinfin:client:app"))
            implementation(project(":pinfin:client:compose"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:client:projector"))
            /*implementation("androidx.test:core-ktx:1.6.1")
            implementation("androidx.test.ext:junit-ktx:1.2.1")
            implementation("androidx.test:runner:1.6.2")
            implementation("com.google.errorprone:error_prone_annotations:2.36.0")*/
        }
    }
}