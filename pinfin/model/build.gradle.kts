import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.model)
                implementation(project(":pinfin:data"))
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.io)
                implementation(libs.ktor.network)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.serialization.cbor)
                implementation(libs.pipe.annotations)
            }
        }
        androidMain
        desktopMain
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
}

tasks.withType<KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
