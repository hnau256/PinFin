import org.gradle.kotlin.dsl.implementation
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
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

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hnau.projector)
            implementation(libs.hnau.model)
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:data"))
            implementation(project(":pinfin:projector"))
            implementation(compose.components.resources)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.serialization.core)
        }

        androidMain {
            dependencies {
                implementation(libs.android.activity.compose)
                implementation(libs.android.appcompat)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}
