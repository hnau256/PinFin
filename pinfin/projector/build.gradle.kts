plugins {
    id("com.google.devtools.ksp")
    id("org.hnau.ui")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
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
