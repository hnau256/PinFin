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
                implementation(project(":model"))
                implementation(project(":data"))
                implementation(libs.kotlin.immutable)
                implementation(libs.bignum)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}
