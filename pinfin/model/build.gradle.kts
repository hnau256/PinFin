plugins {
    id("org.hnau.project")
}

hnau {
    kmp {
        ksp {
            pipe = true
            sealUp = true
            enumValues = true
        }

        implementation(libs.hnau.model)
        implementation(project(":pinfin:data"))
        implementation(libs.kotlin.datetime)
        implementation(libs.kotlin.io)
        implementation(libs.ktor.network)
        implementation(libs.kotlin.serialization.json)
        implementation(libs.kotlin.serialization.cbor)
        implementation(libs.pipe.annotations)
        implementation(libs.sealup.annotations)
        implementation(libs.enumvalues.annotations)
        implementation(libs.bignum)
    }

    serialization = true
}
