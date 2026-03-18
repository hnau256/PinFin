package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal

fun Expression.Companion.parseOrNull(input: String): Expression? = Parser(input).parseOrNull()

private class Parser(
    private val source: String,
) {
    private var pos = 0

    private fun skipWhitespace() {
        while (pos < source.length && source[pos].isWhitespace()) pos++
    }

    fun parseOrNull(): Expression? {
        val expression = parseAddSub() ?: return null
        skipWhitespace()
        return if (pos == source.length) expression else null
    }

    // addSub → mulDiv (('+' | '-') mulDiv)*
    private fun parseAddSub(): Expression? {
        var left = parseMulDiv() ?: return null
        while (true) {
            skipWhitespace()
            if (pos >= source.length) break
            val type =
                when (source[pos]) {
                    '+' -> Expression.BinaryOperation.Type.Plus
                    '-' -> Expression.BinaryOperation.Type.Minus
                    else -> break
                }
            pos++
            val right = parseMulDiv() ?: return null
            left = Expression.BinaryOperation(argument1 = left, argument2 = right, type = type)
        }
        return left
    }

    // mulDiv → unary (('*' | '/') unary)*
    private fun parseMulDiv(): Expression? {
        var left = parseUnary() ?: return null
        while (true) {
            skipWhitespace()
            if (pos >= source.length) break
            val type =
                when (source[pos]) {
                    '*' -> Expression.BinaryOperation.Type.Times
                    '/' -> Expression.BinaryOperation.Type.Divide
                    else -> break
                }
            pos++
            val right = parseUnary() ?: return null
            if (type == Expression.BinaryOperation.Type.Divide) {
                val rightIsZero = right
                    .evaluateOrNull(divisionMode = null)
                    ?.isZero() == true
                if (rightIsZero) {
                    return null
                }
            }
            left = Expression.BinaryOperation(argument1 = left, argument2 = right, type = type)
        }
        return left
    }

    // unary → ('-' | '+') unary | primary
    private fun parseUnary(): Expression? {
        skipWhitespace()
        if (pos >= source.length) return null
        return when (source[pos]) {
            '-' -> {
                pos++
                val argument = parseUnary() ?: return null
                Expression.UnaryOperation(argument = argument, type = Expression.UnaryOperation.Type.Minus)
            }
            '+' -> {
                pos++
                parseUnary()
            }
            else -> parsePrimary()
        }
    }

    // primary → NUMBER | '(' addSub ')'
    private fun parsePrimary(): Expression? {
        skipWhitespace()
        if (pos >= source.length) return null
        return when {
            source[pos] == '(' -> {
                pos++
                val expression = parseAddSub() ?: return null
                skipWhitespace()
                if (pos >= source.length || source[pos] != ')') return null
                pos++
                expression
            }
            source[pos].isDigit() || source[pos] == '.' -> parseNumber()
            else -> null
        }
    }

    private fun parseNumber(): Expression? {
        val start = pos
        while (pos < source.length && (source[pos].isDigit() || source[pos] == '.')) pos++
        val raw = source.substring(start, pos)
        val value = runCatching { BigDecimal.parseString(raw) }.getOrNull() ?: return null
        return Expression.Value(value = value)
    }
}
