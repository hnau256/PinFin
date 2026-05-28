plugins {
    id(hnau.plugins.kotlin.serialization.get().pluginId)
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmp.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.upchain.sync.client.http)
                implementation(libs.bignum) //TODO use from bom
                implementation(hnau.kotlinx.atomicfu)
            }
        }
    }
}
