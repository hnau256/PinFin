package hnau.common.kotlin

fun String.removePrefixOrNull(
    prefix: String,
): String? = when (val nonEmptyPrefix = prefix.takeIf(String::isNotEmpty)) {
    null -> this
    else -> removePrefix(nonEmptyPrefix).takeIf { it != this }
}

fun String.removeSuffixOrNull(
    suffix: String,
): String? = when (val nonEmptySuffix = suffix.takeIf(String::isNotEmpty)) {
    null -> this
    else -> removeSuffix(nonEmptySuffix).takeIf { it != this }
}