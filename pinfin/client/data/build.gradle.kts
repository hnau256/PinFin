plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":pinfin:scheme"))
            implementation(libs.kotlin.datetime)
        }
    }
}
