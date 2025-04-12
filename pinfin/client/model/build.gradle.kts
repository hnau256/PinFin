plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":pinfin:scheme"))
            implementation(project(":pinfin:client:data"))
            implementation(libs.kotlin.datetime)
        }
    }
}
