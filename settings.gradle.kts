rootProject.name = "PinFin"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("org.jetbrains.kotlin.android") version "2.3.10"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
        id("com.google.devtools.ksp") version "2.3.6"
    }
}

plugins {
    id("org.hnau.settings") version "1.0.2"
}

hnauSettings {
    allModules {
        group = "hnau"
        includeHnauCommons = true
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

fun collectSubproject(
    projectDir: File,
    projectIdentifier: String = "",
): List<String> {
    fun collectProjectWithSubprojects(
        dir: File,
        projectIdentifier: String,
    ): List<String> {
        fun checkIsProject(
            dir: File,
            projectIdentifier: String,
        ): Boolean {
            if (projectIdentifier == ":plugins") {
                return false
            }
            if (projectIdentifier == ":buildSrc") {
                return false
            }
            return dir
                .list()
                ?.any { file -> file == "settings.gradle.kts" || file == "build.gradle.kts" }
                ?: false
        }

        return when (checkIsProject(dir, projectIdentifier)) {
            true -> listOf(projectIdentifier)
            false -> collectSubproject(dir, projectIdentifier)
        }
    }

    return projectDir
        .listFiles()
        .orEmpty()
        .filter { it.exists() && it.isDirectory }
        .flatMap { subdir ->
            val subdirName = subdir.name
            collectProjectWithSubprojects(
                dir = subdir,
                projectIdentifier = "$projectIdentifier:$subdirName",
            )
        }
}

include(collectSubproject(rootProject.projectDir))
