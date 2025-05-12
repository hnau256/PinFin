package hnau.common.ktgen

import hnau.common.ktgen.utils.KtFileImpl

inline fun generateKt(
    pkg: String,
    indent: String = "    ",
    block: KtFile.() -> Unit,
): String {
    val lines: MutableList<String> = mutableListOf()
    val imports: MutableList<Importable> = mutableListOf()
    val ktFile = KtFileImpl(
        onNewLine = { line ->
            lines += indent.repeat(line.indents) + line.text
        },
        onNewImport = imports::add,
    )
    ktFile.block()
    val pkgString = "package $pkg"
    val importsString = imports
        .distinct()
        .map { importable -> importable.prefix + "." + importable.suffix }
        .sorted()
        .joinToString(
            separator = "\n",
        ) { importable ->
            "import $importable"
        }
    val linesString = lines.joinToString(
        separator = "\n",
    )
    return listOf(
        pkgString,
        importsString,
        linesString,
    )
        .filter(String::isNotEmpty)
        .joinToString(
        separator = "\n\n"
    )
}