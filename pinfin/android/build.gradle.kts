plugins {
    id("com.google.devtools.ksp")
    id("org.hnau.androidapp")
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
    implementation(libs.hnau.projector)
    implementation(libs.hnau.model)
    implementation(project(":pinfin:app"))
    implementation(project(":pinfin:model"))
    implementation(project(":pinfin:data"))
    implementation(project(":pinfin:projector"))
    implementation(libs.bignum)
}
