import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

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
            applicationIdSuffix = ".debug"
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
            applicationIdSuffix = ".qa"
        }
    }
}

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}

kotlin {
    sourceSets {

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(project(":pinfin:projector"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serialization.core)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
                implementation(libs.enumvalues.annotations)
            }
        }

        val commonJvmMain by creating {
            dependsOn(commonMain)
        }

        androidMain {
           dependsOn(commonJvmMain)
            dependencies {
                implementation(libs.android.activity.compose)
                implementation(libs.android.appcompat)
            }
        }

        desktopMain {
            dependsOn(commonJvmMain)
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
    add("kspCommonMainMetadata", libs.enumvalues.processor)
    add("kspCommonMainMetadata", libs.sealup.processor)
}

tasks.withType<KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}
