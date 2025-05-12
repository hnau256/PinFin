package hnau.common.ktgen

fun KtFile.inject(
    importable: Importable,
): String {
    addImport(importable)
    return importable.suffix
}

fun KtFile.inject(
    prefix: String,
    suffix: String,
): String = inject(
    importable = Importable(
        prefix = prefix,
        suffix = suffix,
    )
)