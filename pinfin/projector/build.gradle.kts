plugins {
    id("org.hnau.project")
}

hnau {
    kmp {
        compose = true

        ksp {
            pipe = true
            sealUp = true
            enumValues = true
        }

        implementation(libs.hnau.projector)
        implementation(libs.hnau.model)
        implementation(libs.hnau.dynamiccolor)
        implementation(project(":pinfin:model"))
        implementation(project(":pinfin:data"))
        implementation(libs.kotlin.datetime)
        implementation(libs.kotlin.immutable)
        implementation(libs.pipe.annotations)
        implementation(libs.sealup.annotations)
        implementation(libs.bignum)
    }
}
