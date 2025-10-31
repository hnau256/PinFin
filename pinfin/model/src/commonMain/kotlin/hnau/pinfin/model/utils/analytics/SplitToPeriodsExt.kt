package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.foldNullable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.collections.plus

fun <T> NonEmptyList<T>.splitToPeriods(
    customStartOfOneOfPeriods: LocalDate?,
    duration: DatePeriod,
    extractDate: (T) -> LocalDate,
): NonEmptyList<Pair<LocalDateRange, List<T>>> {
    val step = duration
    return tail
        .fold(
            initial = run {
                val firstDate = extractDate(head)
                var start = customStartOfOneOfPeriods ?: firstDate
                while (start + step <= firstDate) {
                    start += step
                }
                while (start > firstDate) {
                    start -= step
                }
                val end = start + step - oneDay
                val period = start..end
                val last = period to listOf(head)
                listOf<Pair<LocalDateRange, List<T>>>() to last
            }
        ) { (previous, last), transaction ->
            val (lastRange, lastTransactions) = last
            val date = extractDate(transaction)
            if (date in lastRange) {
                val newLast = lastRange to (lastTransactions + transaction)
                return@fold previous to newLast
            }
            var newLastRange: LocalDateRange = lastRange.offset(step)
            val newPrevious = buildList {
                addAll(previous)
                add(last)
                while (date !in newLastRange) {
                    add(newLastRange to emptyList())
                    newLastRange = newLastRange.offset(step)
                }
            }
            val newLast = newLastRange to listOf(transaction)
            newPrevious to newLast
        }
        .let { (previous, last) ->
            previous
                .toNonEmptyListOrNull()
                .foldNullable(
                    ifNull = { nonEmptyListOf(last) },
                    ifNotNull = { it + last }
                )
        }
}

private val oneDay: DatePeriod = DatePeriod(days = 1)

private fun LocalDateRange.offset(
    period: DatePeriod,
): LocalDateRange = (start + period)..(endInclusive + period)