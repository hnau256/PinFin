package hnau.pinfin.model.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DateRange {

    @Serializable
    @SerialName("after")
    data class After(
        val start: LocalDate,
    ) : DateRange

    @Serializable
    @SerialName("before")
    data class Before(
        val before: LocalDate,
    ) : DateRange

    @Serializable
    @SerialName("inter")
    data class Inter(
        val after: LocalDate,
        val period: DatePeriod,
    ) : DateRange
}