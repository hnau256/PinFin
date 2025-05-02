import com.android.build.gradle.internal.tasks.AppPreBuildTask
import java.util.Properties

plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.app")
    alias(libs.plugins.googleServices)
}

android {
    namespace = "hnau.pinfin"

    defaultConfig {
        val versionPropsFile = file("version.properties")
        val versionProps = Properties().apply {
            load(versionPropsFile.inputStream())
        }
        val localVersionCode = (versionProps["versionCode"] as String).toInt()
        versionName = versionProps["versionName"] as String + "." + localVersionCode
        versionCode = localVersionCode

        tasks.named("preBuild") {
            doFirst {
                versionProps.setProperty("versionCode", (localVersionCode + 1).toString())
                versionProps.store(versionPropsFile.outputStream(), null)
            }
        }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    signingConfigs {
        create("firebase") {
            storeFile = file("keystores/firebase.keystore")
            storePassword = "password"
            keyAlias = "firebase"
            keyPassword = "password"
        }
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFile("proguard-rules.pro")

        }
        create("firebase") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("firebase")
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
            implementation(project(":pinfin:app"))
            implementation(project(":pinfin:compose"))
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:projector"))
            /*implementation("androidx.test:core-ktx:1.6.1")
            implementation("androidx.test.ext:junit-ktx:1.2.1")
            implementation("androidx.test:runner:1.6.2")
            implementation("com.google.errorprone:error_prone_annotations:2.36.0")*/
        }
    }
}