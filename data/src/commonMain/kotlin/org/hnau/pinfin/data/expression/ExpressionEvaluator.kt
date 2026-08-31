package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

fun Expression.evaluate(
    decimalMode: DecimalMode,
): BigDecimal = evaluateOrNull(
    decimalMode = decimalMode,
)!!

internal fun Expression.evaluateOrNull(
    decimalMode: DecimalMode?,
): BigDecimal? = fold(
    ifValue = { it },
    ifUnaryOperation = { argument, type ->
        val right = argument.evaluateOrNull(decimalMode = decimalMode) ?: return null
        type.fold(ifMinus = { right.negate() })
    },
    ifBinaryOperation = { argument1, argument2, type ->
        val left = argument1.evaluateOrNull(decimalMode = decimalMode) ?: return null
        val right = argument2.evaluateOrNull(decimalMode = decimalMode) ?: return null
        type.fold(
            ifPlus = { left + right },
            ifMinus = { left - right },
            ifTimes = { left * right },
            ifDivide = { if (right.isZero()) null else left.divide(right, decimalMode) },
        )
    },
)
