plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    jvmToolchain(17)
    jvm {
        withJava()

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.material3)
            implementation(project(":common:app"))
            implementation(project(":common:color"))
        }
    }
}

