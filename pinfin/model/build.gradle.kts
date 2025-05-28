plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
}

kotlin {
    sourceSets {
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
            }
        }
        androidMain
        desktopMain
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
}
