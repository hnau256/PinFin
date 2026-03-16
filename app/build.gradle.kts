plugins {
    id(hnau.plugins.kotlin.serialization.get().pluginId)
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.ui.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(hnau.commons.app.projector)
                implementation(hnau.commons.app.model)
                implementation(project(":model"))
                implementation(project(":data"))
                implementation(project(":projector"))
                implementation(compose.components.resources)
                implementation(libs.bignum)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.app.DesktopAppKt"
    }
}

compose.resources {
    packageOfResClass = "hnau.pinfin.app"
}
