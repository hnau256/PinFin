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

        implementation(libs.hnau.kotlin)
        implementation(libs.kotlin.datetime)
        implementation(libs.enumvalues.annotations)
        implementation(libs.bignum)
    }

    serialization = true
}
