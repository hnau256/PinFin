plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hnau.model)
            implementation(libs.hnau.color)
            implementation(libs.hnau.compose)
            implementation(project(":pinfin:app"))
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:projector"))
        }
    }
}
