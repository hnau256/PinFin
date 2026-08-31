package org.hnau.pinfin.data.expression

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
sealed interface Expression {

    data class Value(
        val value: BigDecimal,
    ) : Expression

    data class UnaryOperation(
        val argument: Expression,
        val type: Type,
    ) : Expression {

        @Fold
        enum class Type { Minus }
    }

    @ConsistentCopyVisibility
    data class BinaryOperation internal constructor(
        val argument1: Expression,
        val argument2: Expression,
        val type: Type,
    ) : Expression {

        @Fold
        enum class Type { Minus, Plus, Times, Divide }
    }

    companion object {

        val zero: Expression =
            Value(BigDecimal.ZERO)
    }
}
