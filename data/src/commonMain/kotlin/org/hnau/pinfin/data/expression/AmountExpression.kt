package org.hnau.pinfin.data.expression

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.utils.DecimalScale
import org.hnau.pinfin.data.utils.decimalMode

@Serializable(AmountExpression.Serializer::class)
data class AmountExpression(
    val expression: Expression,
) {

    data object Serializer : MappingKSerializer<String, AmountExpression>(
        base = String.serializer(),
        mapper = stringMapper,
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

        fun createOrNull(
            string: String,
        ): AmountExpression? = Expression
            .parseOrNull(string)
            ?.let(::AmountExpression)

        val zero: AmountExpression =
            AmountExpression(Expression.zero)

        private val stringMapper: Mapper<String, AmountExpression> = Expression.stringMapper +
                Mapper(::AmountExpression, AmountExpression::expression)
    }
}