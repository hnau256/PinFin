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
        create("qa") {
            storeFile = file("keystores/qa.keystore")
            storePassword = "password"
            keyAlias = "qa"
            keyPassword = "password"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix =".debug"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFile("proguard-rules.pro")
            //signingConfig = signingConfigs.getByName("release")
        }
        create("qa") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("qa")
            applicationIdSuffix =".qa"
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
            implementation(libs.hnau.model)
            implementation(libs.hnau.projector)
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