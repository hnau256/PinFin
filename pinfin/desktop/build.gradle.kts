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
            implementation(project(":common:compose"))
            implementation(project(":common:app"))
            implementation(project(":pinfin:app"))
            implementation(project(":pinfin:data"))
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
