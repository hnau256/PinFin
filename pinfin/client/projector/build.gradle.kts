plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:scheme"))
            implementation(libs.compose.material3)
        }
    }
}
