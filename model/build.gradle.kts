plugins {
    id(hnau.plugins.kotlin.serialization.get().pluginId)
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmpAndroid.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(hnau.commons.app.model)
                implementation(hnau.kotlinx.serialization.cbor)
                implementation(hnau.kotlinx.serialization.json)
                implementation(libs.bignum)
                implementation(libs.kotlin.io)
                implementation(libs.ktor.network)
                implementation(project(":data"))
            }
        }
    }
}
