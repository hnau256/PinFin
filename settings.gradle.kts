rootProject.name = "PinFin"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.hnau.plugin.settings") version "1.2.10"
}

hnau {
    groupId = "org.hnau.pinfin"
}
