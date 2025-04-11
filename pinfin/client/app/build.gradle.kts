plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:model"))
        }
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:model"))
        }
    }
}
