package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@Serializable
sealed interface PeriodSpec {

    @Serializable
    @SerialName("whole")
    data object Whole : PeriodSpec

    /** N months, starting at day [startDay] (1..31; the last day of a shorter month is used). */
    @Serializable
    @SerialName("months")
    data class Months(
        val count: Int,
        @SerialName("start_day")
        val startDay: Int,
    ) : PeriodSpec

    /** N years, starting at [startMonth]/[startDay]. */
    @Serializable
    @SerialName("years")
    data class Years(
        val count: Int,
        @SerialName("start_month")
        val startMonth: Int,
        @SerialName("start_day")
        val startDay: Int,
    ) : PeriodSpec

    /** N days, one of the bounds of which is [anchor]. */
    @Serializable
    @SerialName("days")
    data class Days(
        val count: Int,
        val anchor: LocalDate,
    ) : PeriodSpec

    companion object {

        val default: PeriodSpec = Whole
    }
}

@Fold
@Serializable
enum class GroupBy {

    @SerialName("category")
    Category,

    @SerialName("account")
    Account,
}

@Serializable
data class SubperiodSpec(
    val count: Int,
    val unit: SubperiodUnit,
)

@Fold
@Serializable
enum class SubperiodUnit {

    @SerialName("day")
    Day,

    @SerialName("month")
    Month,

    @SerialName("year")
    Year,
}

@Fold
@Serializable
sealed interface Aggregation {

    @Serializable
    @SerialName("sum")
    data class Sum(
        val incremental: Boolean = false,
    ) : Aggregation

    @Serializable
    @SerialName("average")
    data class Average(
        val subperiod: SubperiodSpec,
    ) : Aggregation

    companion object {

        val default: Aggregation = Sum()
    }
}
