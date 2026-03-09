plugins {
    kotlin("multiplatform")
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
                implementation(libs.hnau.dynamiccolor)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.immutable)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
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

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}
