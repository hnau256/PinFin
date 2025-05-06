plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":common:color"))
            implementation(project(":common:compose"))
            implementation(project(":pinfin:app"))
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:projector"))
        }
    }
}
