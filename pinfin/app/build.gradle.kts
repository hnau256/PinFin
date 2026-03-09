import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
                implementation(project(":pinfin:projector"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serialization.core)
                implementation(libs.pipe.annotations)
                implementation(libs.sealup.annotations)
                implementation(libs.enumvalues.annotations)
                implementation(libs.bignum)
            }
        }

        desktopMain {
            kotlin.srcDir("build/generated/ksp/metadata/desktopMain/kotlin")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
    add("kspCommonMainMetadata", libs.enumvalues.processor)
    add("kspCommonMainMetadata", libs.sealup.processor)
}

tasks.withType<KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}
