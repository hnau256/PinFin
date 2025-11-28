package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import hnau.common.kotlin.foldBoolean
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus


internal fun LocalDateRange.splitToPeriods(
    duration: DatePeriod,
    startOfOneOfPeriods: LocalDate,
    incremental: Boolean,
): NonEmptyList<LocalDateRange> {

    var startOfFirstPeriod = startOfOneOfPeriods
    while (startOfFirstPeriod + duration <= start) {
        startOfFirstPeriod += duration
    }
    while (startOfFirstPeriod > start) {
        startOfFirstPeriod -= duration
    }

    val tailStarts = buildList {
        var periodStart = startOfFirstPeriod + duration
        while (periodStart <= endInclusive) {
            add(periodStart)
            periodStart += duration
        }
    }

    return nonEmptyListOf(
        startOfFirstPeriod,
        *(tailStarts.toTypedArray()),
    ).map { startOfPeriod ->
        val endOfPeriod = startOfPeriod + duration - oneDay
        val start = incremental.foldBoolean(
            ifTrue = { startOfFirstPeriod },
            ifFalse = { startOfPeriod }
        )
        start..endOfPeriod
    }
}

private val oneDay: DatePeriod = DatePeriod(days = 1)