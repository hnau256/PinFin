plugins {
    kotlin("multiplatform")
    id("org.hnau.ui")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(project(":pinfin:projector"))
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serialization.core)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
                implementation(libs.enumvalues.annotations)
                implementation(libs.bignum)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}
