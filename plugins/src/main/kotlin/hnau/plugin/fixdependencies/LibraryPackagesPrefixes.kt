package hnau.plugin.fixdependencies

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import java.io.File

fun getLibraryPackagesPrefixes(
    rootProject: Project,
): Map<String, List<String>> = File(rootProject.rootDir, "gradle/libs.packages.json")
    .readText()
    .let { content ->
        Json.decodeFromString(
            deserializer = libraryPackagesPrefixesSerializer,
            string = content,
        )
    }

private val libraryPackagesPrefixesSerializer: KSerializer<Map<String, List<String>>> =
    MapSerializer(String.serializer(), ListSerializer(String.serializer()))