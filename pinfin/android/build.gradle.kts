import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    id("com.android.application")
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":pinfin:app"))
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(project(":pinfin:projector"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serialization.core)
                implementation(libs.bignum)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.android.activity.compose)
                implementation(libs.android.appcompat)
            }
        }
    }
}

android {
    namespace = "hnau.pinfin"
    compileSdk =
        libs.versions.androidCompileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.androidMinSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.androidCompileSdk
                .get()
                .toInt()

        val versionPropsFile = file("version.properties")
        val versionProps =
            Properties().apply {
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
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFile("proguard-rules.pro")
        }
        create("qa") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("qa")
            applicationIdSuffix = ".qa"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}
