plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:compose"))
            implementation(project(":common:app"))
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:repository"))
            implementation(compose.components.resources)
            implementation(libs.compose.material.iconsExtended)
            implementation(libs.kotlin.datetime)
        }
    }
}
