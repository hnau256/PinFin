package hnau.pinfin.projector.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface DateTimeFormatter {

    fun formatDate(
        date: LocalDate,
    ): String

    fun formatTime(
        time: LocalTime,
    ): String

    companion object
}