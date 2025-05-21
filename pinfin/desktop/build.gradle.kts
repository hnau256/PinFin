plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.slf4j.simple)
            implementation(compose.runtime)
            implementation(libs.hnau.compose)
            implementation(libs.hnau.model)
            implementation(project(":pinfin:app"))
            implementation(project(":pinfin:compose"))
            implementation(project(":pinfin:projector"))
            implementation(project(":pinfin:model"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.desktop.DesktopAppKt"
    }
}
