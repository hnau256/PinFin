plugins {
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.ui.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(hnau.commons.app.projector)
                implementation(hnau.commons.app.model)
                implementation(libs.hnau.dynamiccolor)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.immutable)
                implementation(libs.bignum)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "hnau.pinfin.projector"
}
