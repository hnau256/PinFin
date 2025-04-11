plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":pinfin:client:projector"))
        }
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
            implementation(project(":common:app"))
            implementation(project(":common:color"))
            implementation(project(":pinfin:client:app"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:client:projector"))
        }
    }
}
