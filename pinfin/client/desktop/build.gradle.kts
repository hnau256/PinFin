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
            implementation(project(":pinfin:client:app"))
            implementation(project(":pinfin:client:data"))
            implementation(project(":pinfin:client:compose"))
            implementation(project(":pinfin:client:projector"))
            implementation(project(":pinfin:client:model"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.pinfin.client.desktop.DesktopAppKt"
        /*nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "hnau.kmptest"
            packageVersion = "1.0.0"
        }*/
    }
}
