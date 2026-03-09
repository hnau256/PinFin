plugins {
    id("org.hnau.project")
}

hnau {
    kmp {
        compose = true
        app = true

        ksp {
            pipe = true
            sealUp = true
            enumValues = true
        }

        implementation(libs.hnau.projector)
        implementation(libs.hnau.model)
        implementation(project(":pinfin:model"))
        implementation(project(":pinfin:data"))
        implementation(project(":pinfin:projector"))
        implementation(libs.kotlin.datetime)
        implementation(libs.kotlin.serialization.core)
        implementation(libs.pipe.annotations)
        implementation(libs.sealup.annotations)
        implementation(libs.enumvalues.annotations)
        implementation(libs.bignum)
    }
}
