plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.hnau.ui")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":pinfin:model"))
                implementation(project(":pinfin:data"))
                implementation(project(":pinfin:projector"))
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
