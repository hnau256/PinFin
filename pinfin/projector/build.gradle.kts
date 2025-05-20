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
            implementation(libs.hnau.compose)
            implementation(libs.hnau.app)
            implementation(project(":pinfin:model"))
            implementation(project(":pinfin:data"))
            implementation(compose.components.resources)
            implementation(libs.kotlin.datetime)
        }
    }
}
