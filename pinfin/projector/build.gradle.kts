plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.hnau.ui")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(libs.hnau.dynamiccolor)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.immutable)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
                implementation(libs.bignum)
            }
        }
    }
}
