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
                val last = start to nonEmptyListOf(head)
                listOf<Pair<LocalDate, List<T>>>() to last
            }
        ) { (previous, last), item ->
            val (lastRangeStart, lastTransactions) = last
            val lastRange = lastRangeStart.toRange(duration)
            val date = extractDate(item)
            if (date in lastRange) {
                val newTransactions = lastTransactions.toNonEmptyListOrNull().foldNullable(
                    ifNull = { nonEmptyListOf(item) },
                    ifNotNull = { it + item },
                )
                val newLast = lastRangeStart to newTransactions
                return@fold previous to newLast
            }
            var newLastRangeStart: LocalDate = lastRangeStart + step
            val newPrevious = buildList {
                addAll(previous)
                add(last)
                while (date !in newLastRangeStart.toRange(duration)) {
                    add(newLastRangeStart to emptyList())
                    newLastRangeStart += duration
                }
            }
            val newLast = newLastRangeStart to nonEmptyListOf(item)
            newPrevious to newLast
        }
        .let { (previous, last) ->
            previous
                .toNonEmptyListOrNull()
                .foldNullable(
                    ifNull = { nonEmptyListOf(last) },
                    ifNotNull = { it + last }
                )
                .map { (rangeStart, items) ->
                    val range = rangeStart.toRange(duration)
                    range to items
                }
        }
}

private val oneDay: DatePeriod = DatePeriod(days = 1)

private fun LocalDate.toRange(
    duration: DatePeriod,
): LocalDateRange = this..(this + duration - oneDay)