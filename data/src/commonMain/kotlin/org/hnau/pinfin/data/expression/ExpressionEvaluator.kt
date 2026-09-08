package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifFalse
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
        argument
            .evaluateOrNull(decimalMode = decimalMode)
            ?.let { right ->
                type.fold(ifMinus = { right.negate() })
            }
    },
    ifBinaryOperation = { argument1, argument2, type ->
        argument1
            .evaluateOrNull(decimalMode = decimalMode)
            ?.let { left ->
                argument2
                    .evaluateOrNull(decimalMode = decimalMode)
                    ?.let { right ->
                        type.fold(
                            ifPlus = { left + right },
                            ifMinus = { left - right },
                            ifTimes = { left * right },
                            ifDivide = {
                                right.isZero().ifFalse {
                                    // decimalMode == null только при пре-парсинговой проверке "делитель не ноль"
                                    // (см. ExpressionParser) - там точность результата не важна.
                                    decimalMode.foldNullable(
                                        ifNull = { left.divide(right, decimalMode) },
                                        ifNotNull = { mode -> left.dividePrecisely(right, mode) },
                                    )
                                }
                            },
                        )
                    }
            }
    },
)
