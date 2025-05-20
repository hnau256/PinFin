package hnau.generateicons

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import hnau.common.gen.kt.KtFile
import hnau.common.gen.kt.generateKt
import java.io.File

internal inline fun createKtFile(
    projectName: String,
    subpackage: String,
    name: String,
    block: KtFile.() -> Unit,
) {
    val projectParts = projectName.split(':').filter(String::isNotEmpty)
    val content = "//This file is generated\n\n" + generateKt(
        pkg = projectParts.joinToString(
            prefix = "hnau.",
            separator = ".",
            postfix = ".$subpackage",
        ),
        block = block,
    )
    val file = File(
        nonEmptyListOf(
            projectParts.joinToString("/"),
            "src/commonMain/kotlin/hnau",
            projectParts.joinToString("/"),
            subpackage.replace('.', '/'),
            "$name.kt",
        )
    )
    file.parentFile.mkdirs()
    file.writeText(content)
}

@PublishedApi
internal fun File(
    segments: NonEmptyList<String>,
): File = segments.tail.fold(
    initial = File(segments.head)
) { acc, segment ->
    File(acc, segment)
}