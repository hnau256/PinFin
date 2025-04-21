plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.ooxml)
    implementation(project(":common:kotlin"))
    implementation(project(":pinfin:scheme"))
}

application {
    mainClass = "hnau.finpixconverter.FinPixConverterKt"
}

/*app

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("hnau.finpixconverter.FinPixConverterKt")
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
            implementation(libs.ooxml)
            implementation(project(":pinfin:scheme"))
        }
    }
}*/
