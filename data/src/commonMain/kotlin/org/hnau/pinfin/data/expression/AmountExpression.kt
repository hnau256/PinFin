package org.hnau.pinfin.data.expression

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.utils.DecimalScale
import org.hnau.pinfin.data.utils.decimalMode

@Serializable(AmountExpression.Serializer::class)
class AmountExpression @Deprecated("Use createOrNull") constructor(
    val expression: Expression,
) {

    data object Serializer : MappingKSerializer<String, AmountExpression>(
        base = String.serializer(),
        mapper = Mapper(
            direct = { string ->
                @Suppress("DEPRECATION")
                createOrNullUnsafe(string)
                    ?: error("Unable parse arithmetic expression from '$string'")
            },
            reverse = { amountExpression ->
                amountExpression.expression.serialize()
            },
        ),
    )

    private val amountCacheSync = SynchronizedObject()
    private var amountCache: Pair<DecimalScale, Amount>? = null

    fun toAmount(
        scale: DecimalScale,
    ): Amount = synchronized(amountCacheSync) {
        var result = amountCache
            ?.takeIf { cache ->
                cache.first == scale
            }
            ?.second
        if (result == null) {
            result = expression
                .evaluate(
                    decimalMode = scale.decimalMode,
                )
                .let(::Amount)
            amountCache = scale to result
        }
        result
    }

    companion object {

        @Deprecated("Use createOrNull instead")
        fun createOrNullUnsafe(
            string: String,
        ): AmountExpression? = Expression
            .parseOrNull(string)
            ?.let(::AmountExpression)

        fun createOrNull(
            string: String,
            currency: Currency,
        ): AmountExpression? = createOrNullUnsafe(
            string = string,
        )
            ?.takeIf {
                it
                    .expression
                    .evaluate(currency.scale.decimalMode) >= BigDecimal.ZERO
            }

        val zero: AmountExpression =
            AmountExpression(Expression.zero)
    }
}