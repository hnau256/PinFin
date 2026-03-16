plugins {
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.androidapp.get().pluginId)
}

android {
    namespace = "hnau.pinfin.android"

    defaultConfig {
        applicationId = "hnau.pinfin"
        versionCode = 1
        versionName = "1.0.0"
    }

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
}

dependencies {
    implementation(hnau.commons.app.projector)
    implementation(hnau.commons.app.model)
    implementation(project(":app"))
    implementation(project(":model"))
    implementation(project(":data"))
    implementation(project(":projector"))
    implementation(libs.bignum)
}
