package org.hnau.pinfin.projector.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.hnau.pinfin.projector.Localization

fun createDateTimeFormatter(
    localization: Localization,
): DateTimeFormatter = CommonDateTimeFormatter(
    localization = localization,
)

private class CommonDateTimeFormatter(
    localization: Localization,
) : DateTimeFormatter {

    private val dateFormat: DateTimeFormat<LocalDate> = LocalDate.Format {
        day(padding = Padding.ZERO)
        char(' ')
        monthName(localization.monthNamesAbbreviated)
        char(' ')
        year()
    }

    private val timeFormat = LocalTime.Format {
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    }

    override fun formatDate(
        date: LocalDate,
    ): String = dateFormat.format(date)

    override fun formatTime(
        time: LocalTime,
    ): String = timeFormat.format(time)
}
