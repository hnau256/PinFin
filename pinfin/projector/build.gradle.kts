plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
}

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
}
