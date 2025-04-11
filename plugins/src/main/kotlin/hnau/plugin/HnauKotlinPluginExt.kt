package hnau.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.tasks.testing.Test
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal enum class AndroidMode { Lib, App }

private const val CommonKotlinProjectIdentifier = ":common:kotlin"
private const val CommonComposeProjectIdentifier = ":common:compose"

internal fun Project.config(
    androidMode: AndroidMode?,
) {
    val useAndroid = androidMode != null

    val versions: VersionCatalog = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    val javaVersion: JavaVersion = versions
        .requireVersion("java")
        .let(JavaVersion::valueOf)

    plugins.apply("org.jetbrains.kotlin.multiplatform")
    when (androidMode) {
        AndroidMode.Lib -> plugins.apply("com.android.library")
        AndroidMode.App -> plugins.apply("com.android.application")
        null -> {}
    }

    val hasSerializationPlugin =
        project.plugins.hasPlugin("org.jetbrains.kotlin.plugin.serialization")

    val hasComposePlugin =
        project.plugins.hasPlugin("org.jetbrains.compose")

    val hasKspPlugin =
        project.plugins.hasPlugin("com.google.devtools.ksp")

    extensions.configure(KotlinMultiplatformExtension::class.java) { extension ->

        if (useAndroid) {
            extension.androidTarget {
                compilations.configureEach { jvmCompilation ->
                    jvmCompilation.kotlinOptions {
                        jvmTarget = javaVersion.toString()
                    }
                }
            }
        }
        extension.jvm {
            compilations.configureEach { jvmCompilation ->
                jvmCompilation.kotlinOptions {
                    jvmTarget = javaVersion.toString()
                }
            }
        }
        val sourceSets = listOfNotNull(
            "commonMain",
            "jvmMain",
            "androidMain".takeIf { useAndroid },
        )
        sourceSets.forEach { sourceSetName ->
            val sourceSet = extension.sourceSets.getByName(sourceSetName)
            sourceSet.languageSettings.enableLanguageFeature("ContextReceivers")
            sourceSet.dependencies {
                implementation(versions.findLibrary("arrow-core").get().get())
                implementation(versions.findLibrary("arrow-coroutines").get().get())
                implementation(versions.findLibrary("kotlin-coroutines-core").get().get())
                if (hasSerializationPlugin) {
                    implementation(versions.findLibrary("arrow-serialization").get().get())
                    implementation(versions.findLibrary("kotlin-serialization-core").get().get())
                    implementation(versions.findLibrary("kotlin-serialization-json").get().get())
                }
                if (hasComposePlugin) {
                    implementation(ComposePlugin.DesktopDependencies.currentOs)
                    if (identitifer != CommonComposeProjectIdentifier) {
                        implementation(project(CommonComposeProjectIdentifier))
                    }
                }
                if (hasKspPlugin) {
                    implementation(versions.findLibrary("shuffler-annotations").get().get())
                }
                if (identitifer != CommonKotlinProjectIdentifier) {
                    implementation(project(CommonKotlinProjectIdentifier))
                }
            }
        }
    }

    if (hasKspPlugin) {
        val kspProcessor = versions.findLibrary("shuffler-processor").get().get()
        dependencies.add("kspCommonMainMetadata", kspProcessor)
        dependencies.add("kspJvm", kspProcessor)
        dependencies.add("kspJvmTest", kspProcessor)
        if (useAndroid) {
            dependencies.add("kspAndroid", kspProcessor)
        }
    }

    versions.findLibrary("kotest-framework-engine").get().get().let {
        dependencies.add("commonTestImplementation", it)
    }

    versions.findLibrary("kotest-junit-runner").get().get().let {
        val configurationName = when (useAndroid) {
            true -> "androidUnitTestImplementation"
            false -> "jvmTestImplementation"
        }
        dependencies.add(configurationName, it)
    }

    tasks.withType(Test::class.java).configureEach { testTask ->
        testTask.useJUnitPlatform()
    }

    if (useAndroid) {
        extensions.configure(BaseExtension::class.java) { extension ->

            val compileSdk = versions.requireVersion("androidCompileSdk").toInt()
            val minSdk = versions.requireVersion("androidMinSdk").toInt()
            extension.compileSdkVersion(compileSdk)
            extension.buildToolsVersion(versions.requireVersion("androidBuildTools"))

            extension.defaultConfig { config ->
                config.minSdk = minSdk
                config.targetSdk = compileSdk
            }

            extension.compileOptions { options ->
                options.targetCompatibility = javaVersion
                options.sourceCompatibility = javaVersion
            }
            extension.namespace = "hnau." + path.drop(1).replace(':', '.')
        }
    }
}

private val Project.identitifer: String
    get() = (project as DefaultProject).identityPath.toString()


private fun VersionCatalog.requireVersion(
    alias: String,
): String = findVersion(alias)
    .get()
    .requiredVersion