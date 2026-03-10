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
    id("org.hnau.settings") version "1.2.0"
}

hnau {
    groupId = "org.hnau.pinfin"
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
