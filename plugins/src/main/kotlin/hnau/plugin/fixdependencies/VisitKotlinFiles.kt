package hnau.plugin.fixdependencies

import hnau.plugin.fixdependencies.path.Path
import hnau.plugin.fixdependencies.path.filePath
import hnau.plugin.fixdependencies.path.moduleIdentifier
import hnau.plugin.fixdependencies.path.packagePrefix
import java.io.File

fun visitKotlinFiles(
    projectRoot: File,
    moduleIdentifier: Path,
    onKotlinFile: (List<String>) -> Unit,
) {
    val expectedKtFilePathPrefix = "hnau/" + moduleIdentifier.filePath
    val expectedKtFileFirstLinePrefix = "package ${moduleIdentifier.packagePrefix}"
    val srcDir = File(projectRoot, "src")
    if (!srcDir.exists()) {
        return
    }
    srcDir
        .listFiles()
        .orEmpty()
        .onEach { sourceSetFile ->
            val name = sourceSetFile.name
            if (!sourceSetFile.isDirectory) {
                throwError(
                    moduleIdentifier = moduleIdentifier,
                    message = "Source set '$name' must be directory",
                )
            }
            if (name !in availableSourceSets) {
                throwError(
                    moduleIdentifier = moduleIdentifier,
                    message = "Unknown source set '$name'",
                )
            }
        }
        .mapNotNull { sourceSetDir ->
            File(sourceSetDir, "kotlin").takeIf { it.exists() }
        }
        .forEach { kotlinDir ->
            val kotlinDirPath = kotlinDir.absolutePath
            kotlinDir.forEachFile { file ->
                if (file.extension != "kt") {
                    return@forEachFile
                }
                val absolutePath = file.absolutePath
                val relativePath = absolutePath.removePrefix(kotlinDirPath).drop(1)
                if (!relativePath.startsWith(expectedKtFilePathPrefix)) {
                    throwError(
                        moduleIdentifier = moduleIdentifier,
                        message = "Kotlin file '$absolutePath' not in `$kotlinDirPath/$expectedKtFilePathPrefix`",
                    )
                }
                val lines = file.readLines()
                if (lines.isEmpty()) {
                    throwError(
                        moduleIdentifier = moduleIdentifier,
                        message = "Kotlin file '$absolutePath' is empty",
                    )
                }
                val firstLine = lines.first()
                if (!firstLine.startsWith(expectedKtFileFirstLinePrefix)) {
                    throwError(
                        moduleIdentifier = moduleIdentifier,
                        message = "Kotlin file '$absolutePath' first line is `$firstLine`. Prefix `$expectedKtFileFirstLinePrefix` expected",
                    )
                }
                onKotlinFile(lines)
            }
        }
    File(projectRoot, "build/generated")
        .takeIf { it.exists() }
        ?.forEachFile { file ->
            if (file.extension != "kt") {
                return@forEachFile
            }
            val lines = file.readLines()
            onKotlinFile(lines)
        }
}

private fun File.forEachFile(
    action: (File) -> Unit,
) {
    when (isDirectory) {
        false -> action(this)
        true -> listFiles()
            .orEmpty()
            .forEach { child ->
                child.forEachFile(action)
            }
    }
}

private fun throwError(
    moduleIdentifier: Path,
    message: String,
): Nothing {
    error("Error in module `${moduleIdentifier.moduleIdentifier}`: $message")
}

private val availableSourceSets: Set<String> =
    setOf("commonMain", "jvmMain", "androidMain", "commonTest")
