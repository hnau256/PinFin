package hnau.common.kotlin.mapper

import hnau.common.kotlin.ifNull
import hnau.common.kotlin.joinEscaped
import hnau.common.kotlin.splitEscaped

fun Mapper.Companion.stringToStringsPairBySeparator(
    separator: Char,
    escape: Char = '\\',
) = Mapper<String, Pair<String, String>>(
    direct = { string ->
        string
            .splitEscaped(separator, escape)
            .takeIf { parts -> parts.size == 2 }
            .ifNull { throw IllegalArgumentException("Unable split $string to 2 parts by separator $separator") }
            .let { (first, second) ->
                first to second
            }
    },
    reverse = { keyWithValue ->
        keyWithValue.toList().joinEscaped(separator, escape)
    },
)
