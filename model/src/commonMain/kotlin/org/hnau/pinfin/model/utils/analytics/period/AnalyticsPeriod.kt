@file:UseSerializers(
    MonthSerializer::class,
)

package org.hnau.pinfin.model.utils.analytics.period

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.serializers.MonthSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@Serializable
sealed interface AnalyticsPeriod {

    /** Все транзакции одним периодом. */
    @Serializable
    @SerialName("whole")
    data object Whole : AnalyticsPeriod

    /** N месяцев, начиная с числа startDay (1..31, при коротком месяце — его последний день). */
    @Serializable
    @SerialName("months")
    data class Months(
        val count: Int,      // ≥ 1; 1 = месяц, 3 = квартал
        val startDay: Int,   // 1..31
    ) : AnalyticsPeriod

    /** N лет, начиная с месяца и числа (например 1 марта). */
    @Serializable
    @SerialName("years")
    data class Years(
        val count: Int,
        val startMonth: Month,
        val startDay: Int,   // 1..31, для февраля клампится к 28/29
    ) : AnalyticsPeriod

    /** N дней от конкретной даты (7 дней от понедельника = неделя). */
    @Serializable
    @SerialName("days")
    data class Days(
        val count: Int,
        val anchor: LocalDate,   // реальная дата, выбранная пользователем; дефолт — дата первой транзакции
    ) : AnalyticsPeriod
}
