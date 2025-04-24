package hnau.common.kotlin.mapper

import hnau.common.kotlin.ifNull

fun Mapper.Companion.stringToStringsPairBySeparator(
    separator: Char,
    escape: Char = '\\',
): Mapper<String, Pair<String, String>> = Mapper.stringToStringsBySeparator(
    separator = separator,
    escape = escape,
) + Mapper<List<String>, Pair<String, String>>(
    direct = { list ->
        list
            .takeIf { parts -> parts.size == 2 }
            .ifNull {
                val source = list.joinToString(separator = separator.toString())
                throw IllegalArgumentException("Expected 2 parts, got `$source`")
            }
            .let { (first, second) ->
                first to second
            }
    },
    reverse = Pair<String, String>::toList,
)