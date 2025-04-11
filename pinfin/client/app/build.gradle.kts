plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":pinfin:client:model"))
            implementation(project(":pinfin:client:data"))
        }
    }
}
