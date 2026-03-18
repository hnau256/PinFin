package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

fun Expression.evaluate(
    divisionMode: DecimalMode,
): BigDecimal = evaluateOrNull(
    divisionMode = divisionMode,
)!!

internal fun Expression.evaluateOrNull(
    divisionMode: DecimalMode?,
): BigDecimal? = when (this) {
    is Expression.Value -> value
    is Expression.UnaryOperation -> {
        val right = argument.evaluateOrNull(divisionMode = divisionMode) ?: return null
        when (type) {
            Expression.UnaryOperation.Type.Minus -> right.negate()
        }
    }

    is Expression.BinaryOperation -> {
        val left = argument1.evaluateOrNull(divisionMode = divisionMode) ?: return null
        val right = argument2.evaluateOrNull(divisionMode = divisionMode) ?: return null
        when (type) {
            Expression.BinaryOperation.Type.Plus -> left + right
            Expression.BinaryOperation.Type.Minus -> left - right
            Expression.BinaryOperation.Type.Times -> left * right
            Expression.BinaryOperation.Type.Divide -> {
                if (right.isZero()) null else left.divide(right, divisionMode)
            }
        }
    }
}
