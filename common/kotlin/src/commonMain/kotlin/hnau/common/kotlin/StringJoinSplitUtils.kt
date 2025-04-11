package hnau.common.kotlin

fun List<String>.joinEscaped(
    separator: Char,
    escape: Char = '\\',
) = joinToString(
    separator = separator.toString(),
) { string ->
    string
        .replace(escape.toString(), "$escape$escape")
        .replace(separator.toString(), "$escape$separator")
}

fun String.splitEscaped(
    separator: Char,
    escape: Char = '\\',
): List<String> {
    if (this.isEmpty()) {
        return emptyList()
    }
    val result = mutableListOf<String>()
    var temp = ""
    var lastWasEscape = false
    forEach { char ->
        if (lastWasEscape) {
            temp += char
            lastWasEscape = false
            return@forEach
        }
        var nextLastWasEscape = false
        when (char) {
            separator -> {
                result += temp
                temp = ""
            }

            escape -> nextLastWasEscape = true
            else -> temp += char
        }
        lastWasEscape = nextLastWasEscape
    }
    result += temp
    return result
}
