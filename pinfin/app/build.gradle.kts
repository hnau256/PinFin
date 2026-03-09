plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.ui")
}

kotlin {
    sourceSets {
        commonMain {
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
                implementation(libs.bignum)
            }
        }

        androidMain {
            kotlin.srcDir("build/generated/ksp/metadata/androidMain/kotlin")
        }

        val desktopMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/desktopMain/kotlin")
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}

compose.resources {
    packageOfResClass = "hnau.pinfin.app"
}
