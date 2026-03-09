plugins {
    id("org.hnau.project")
}

hnau {
    androidApp(
        namespace = "hnau.pinfin",
        qaKeystorePath = "keystores/qa.keystore",
        qaKeyAlias = "qa",
        qaStorePassword = "password",
        qaKeyPassword = "password",
    ) {
        implementation(project(":pinfin:app"))
        implementation(project(":pinfin:model"))
        implementation(project(":pinfin:data"))
        implementation(project(":pinfin:projector"))

        implementation(libs.hnau.projector)
        implementation(libs.hnau.model)

        implementation(libs.kotlin.datetime)
        implementation(libs.kotlin.serialization.core)
        implementation(libs.bignum)

        implementation(libs.android.activity.compose)
        implementation(libs.android.appcompat)
    }
}
