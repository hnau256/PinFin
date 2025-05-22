plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hnau.model)
            implementation(libs.hnau.projector)
            implementation(project(":pinfin:model"))
        }
    }
}
