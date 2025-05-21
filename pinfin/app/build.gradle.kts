plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hnau.model)
            implementation(project(":pinfin:model"))
            implementation(libs.kotlin.serialization.json)
        }
    }
}
