import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
}

kotlin {
    linuxX64()
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.kotlin)
                implementation(libs.kotlin.datetime)
                implementation(libs.enumvalues.annotations)
            }
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
