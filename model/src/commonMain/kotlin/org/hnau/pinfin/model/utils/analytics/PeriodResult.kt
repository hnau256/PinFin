package org.hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.AmountDirectionValues
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.model.filter.Filters

/** Результат [calcPeriod] — суммы по группам за один период страницы. */
data class PeriodResult(
    val values: AmountDirectionValues<Half?>,
) {

    data class Half(
        val values: NonEmptyList<KeyValue<GroupKey, Value>>,
    ) {

        data class Value(
            val amount: Amount,
            val filters: Filters,
        )

        private val amounts: NonEmptyList<Amount> = values
            .map { item -> item.value.amount }

        val max: Amount = amounts.max()

        val sum: Amount = amounts.sum()
    }

    val total: KeyValue<AmountDirection, Amount>? = AmountDirection
        .entries
        .mapNotNull { direction ->
            values[direction]?.let { half ->
                KeyValue(
                    key = direction,
                    value = half.sum,
                )
            }
        }
        .toNonEmptyListOrNull()
        ?.sum()
}
