package hnau.pinfin.projector.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter as DateTimeFormatterFromJava

class JavaDateTimeFormatter(
    datePattern: String = "dd MMM yyyy",
    timePattern: String = "HH:mm",
): DateTimeFormatter {

    private val dateFormatter: DateTimeFormatterFromJava =
        DateTimeFormatterFromJava.ofPattern(datePattern)

    private val timeFormatter: DateTimeFormatterFromJava =
        DateTimeFormatterFromJava.ofPattern(timePattern)

    override fun formatDate(
        date: LocalDate,
    ): String = date
        .toJavaLocalDate()
        .format(dateFormatter)

    override fun formatTime(
        time: LocalTime,
    ): String = time
        .toJavaLocalTime()
        .format(timeFormatter)
}