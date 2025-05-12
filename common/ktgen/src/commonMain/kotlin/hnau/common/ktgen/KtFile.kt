package hnau.common.ktgen

interface KtFile {

    operator fun String.unaryPlus()

    fun addImport(
        importable: Importable,
    )

    fun indent(
        block: KtFile.() -> Unit,
    )
}