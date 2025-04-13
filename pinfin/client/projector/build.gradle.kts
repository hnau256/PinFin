plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:compose"))
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:client:data"))
            implementation(project(":pinfin:scheme"))
            implementation(compose.components.resources)
            implementation(libs.compose.material.iconsExtended)
            implementation(libs.kotlin.datetime)
        }
    }
}
