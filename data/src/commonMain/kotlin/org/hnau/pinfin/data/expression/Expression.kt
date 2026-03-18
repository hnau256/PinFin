package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal

sealed interface Expression {
    companion object;

    @ConsistentCopyVisibility
    data class Value internal constructor(
        val value: BigDecimal,
    ) : Expression

    @ConsistentCopyVisibility
    data class UnaryOperation internal constructor(
        val argument: Expression,
        val type: Type,
    ) : Expression {
        enum class Type { Minus }
    }

    @ConsistentCopyVisibility
    data class BinaryOperation internal constructor(
        val argument1: Expression,
        val argument2: Expression,
        val type: Type,
    ) : Expression {
        enum class Type { Minus, Plus, Times, Divide }
    }
}
