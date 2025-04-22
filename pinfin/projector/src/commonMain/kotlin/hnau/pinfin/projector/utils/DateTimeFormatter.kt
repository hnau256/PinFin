package hnau.pinfin.projector.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface DateTimeFormatter {

    fun formatDate(
        date: LocalDate,
    ): String

    fun formatTime(
        time: LocalTime,
    ): String

    companion object {

        val test: DateTimeFormatter = object : DateTimeFormatter {

            override fun formatDate(
                date: LocalDate,
            ): String = date.toString()

            override fun formatTime(
                time: LocalTime,
            ): String = time
                .toString()
                .takeWhile { it != '.' }
        }
    }

}