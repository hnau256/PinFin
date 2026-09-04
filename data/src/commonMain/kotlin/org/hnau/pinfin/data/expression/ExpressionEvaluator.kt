package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import org.hnau.pinfin.data.utils.dividePrecisely

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
            ifDivide = {
                if (right.isZero()) {
                    null
                } else {
                    // decimalMode == null только при пре-парсинговой проверке "делитель не ноль"
                    // (см. ExpressionParser) - там точность результата не важна.
                    decimalMode
                        ?.let { mode -> left.dividePrecisely(right, mode) }
                        ?: left.divide(right, decimalMode)
                }
            },
        )
    },
)
