plugins {
    id(
        hnau.plugins.kotlin.serialization
            .get()
            .pluginId,
    )
    id(
        hnau.plugins.ksp
            .get()
            .pluginId,
    )
    id(
        hnau.plugins.hnau.ui
            .get()
            .pluginId,
    )
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
                implementation(libs.bignum)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.hnau.pinfin.app.DesktopAppKt"
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    enableStrongSkippingMode = true
}
