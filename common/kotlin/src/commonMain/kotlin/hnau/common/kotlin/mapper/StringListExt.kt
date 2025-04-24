package hnau.common.kotlin.mapper

import hnau.common.kotlin.joinEscaped
import hnau.common.kotlin.splitEscaped

fun Mapper.Companion.stringToStringsBySeparator(
    separator: Char,
    escape: Char = '\\',
): Mapper<String, List<String>> = Mapper<String, List<String>>(
    direct = { string ->
        string
            .splitEscaped(separator, escape)
    },
    reverse = { keyWithValue ->
        keyWithValue.joinEscaped(separator, escape)
    },
)
