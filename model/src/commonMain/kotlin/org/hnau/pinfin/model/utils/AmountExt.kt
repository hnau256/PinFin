package org.hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.data.records.FilteredRecord
import org.hnau.pinfin.data.records.RecordEntry
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlin.jvm.JvmName


@JvmName("unresolvedTotalAmount")
fun NonEmptyList<RecordEntry<CategoryId>>.totalAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = map { entry ->
    KeyValue(entry.record.category.direction, entry.record.amount)
}.amount(currency)

fun NonEmptyList<RecordEntry<KeyValue<CategoryId, CategoryInfo>>>.totalAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = map { entry ->
    KeyValue(entry.record.resolvedDirection, entry.record.amount)
}.amount(currency)

fun NonEmptyList<FilteredRecord<KeyValue<CategoryId, CategoryInfo>>>.filteredAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = includedRecords()
    .map { record -> KeyValue(record.resolvedDirection, record.amount) }
    .amount(currency)

fun <C> NonEmptyList<FilteredRecord<C>>.includedRecords(): NonEmptyList<Record<C>> = filter { it.included }
    .map { it.record }
    .toNonEmptyListOrThrow()

fun <C> NonEmptyList<FilteredRecord<C>>.additionalRecords(): List<Record<C>> = filterNot { it.included }
    .map { it.record }

val Record<KeyValue<CategoryId, CategoryInfo>>.resolvedDirection: AmountDirection
    get() = category.key.direction

private fun NonEmptyList<KeyValue<AmountDirection, AmountExpression>>.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = tail.fold(
    initial = head.map { it.toAmount(currency.scale) },
) { acc, record ->
    acc + record.map { it.toAmount(currency.scale) }
}
