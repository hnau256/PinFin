plugins {
    id(hnau.plugins.kotlin.serialization.get().pluginId)
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmp.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(hnau.commons.app.model)
                implementation(project(":pinfin:data"))
                implementation(libs.kotlin.io)
                implementation(libs.ktor.network)
                implementation(libs.bignum)
            }
        }
    }
}
