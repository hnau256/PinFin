@file:UseSerializers(
    MutableStateFlowSerializer::class,
    MonthSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.previousOrSame
import kotlinx.datetime.serializers.MonthSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit

/**
 * Состояние якоря начала периода (docs/analytics-v2-plan.md, "2.2. Как пользователь задаёт
 * начало периода"): хранит все три представления одновременно (день месяца / месяц+день /
 * дата), а какое из них показывать и редактировать решает [PeriodConfigModel] по текущей
 * единице ([unit]) - ровно как задаёт таблица контролов в плане.
 *
 * Единица хранения не связана с count - поэтому переключение количества в пределах одной
 * единицы (месяц -> квартал, 7 -> 14 дней) не трогает якорь: он и так один и тот же
 * [StateFlow]. Сброс на дефолт единицы происходит только при смене самой единицы
 * ([unit] - действующая единица периода, реагирует и на пресеты, и на "Свой").
 */
class AnchorModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    val unit: StateFlow<PeriodUnit>,
    private val count: StateFlow<Int>,
    private val firstTransactionDate: LocalDate,
    private val lastTransactionDate: LocalDate,
) {

    @Serializable
    data class Skeleton(
        val monthsStartDay: MutableStateFlow<Int>,
        val yearsStartMonth: MutableStateFlow<Month>,
        val yearsStartDay: MutableStateFlow<Int>,
        val daysAnchor: MutableStateFlow<LocalDate>,
    ) {

        companion object {

            fun create(
                daysAnchor: LocalDate,
                monthsStartDay: Int = 1,
                yearsStartMonth: Month = Month.JANUARY,
                yearsStartDay: Int = 1,
            ): Skeleton = Skeleton(
                monthsStartDay = monthsStartDay.toMutableStateFlowAsInitial(),
                yearsStartMonth = yearsStartMonth.toMutableStateFlowAsInitial(),
                yearsStartDay = yearsStartDay.toMutableStateFlowAsInitial(),
                daysAnchor = daysAnchor.toMutableStateFlowAsInitial(),
            )
        }
    }

    val monthsStartDay: MutableStateFlow<Int>
        get() = skeleton.monthsStartDay

    val yearsStartMonth: MutableStateFlow<Month>
        get() = skeleton.yearsStartMonth

    val yearsStartDay: MutableStateFlow<Int>
        get() = skeleton.yearsStartDay

    val daysAnchor: MutableStateFlow<LocalDate>
        get() = skeleton.daysAnchor

    /** Кратно ли текущее число дней неделе - тогда контрол "день недели", иначе календарь. */
    val isWeekdayMode: StateFlow<Boolean> = count.mapState(scope) { count -> count % 7 == 0 }

    init {
        scope.launch {
            var previous = unit.value
            unit.collect { newUnit ->
                if (newUnit != previous) {
                    resetToDefault(newUnit)
                }
                previous = newUnit
            }
        }
    }

    /** Выбор дня недели в режиме "кратно 7" - якорь = ближайшая такая дата не позже первой транзакции. */
    fun selectWeekday(
        dayOfWeek: DayOfWeek,
    ) {
        skeleton.daysAnchor.value = firstTransactionDate.previousOrSame(dayOfWeek)
    }

    val weekday: StateFlow<DayOfWeek> = derivedStateFlowOf(scope) {
        skeleton.daysAnchor.state.dayOfWeek
    }

    private fun resetToDefault(
        unit: PeriodUnit,
    ) {
        when (unit) {
            PeriodUnit.Month -> skeleton.monthsStartDay.value = 1
            PeriodUnit.Year -> {
                skeleton.yearsStartMonth.value = Month.JANUARY
                skeleton.yearsStartDay.value = 1
            }
            PeriodUnit.Day -> skeleton.daysAnchor.value = defaultDaysAnchor(
                count = count.value,
                firstTransactionDate = firstTransactionDate,
                lastTransactionDate = lastTransactionDate,
            )
        }
    }
}

/**
 * Дефолт якоря при сбросе на единицу "дни" (docs/analytics-v2-plan.md, "2.2"): кратно 7 -
 * ближайший понедельник не позже первой транзакции, иначе - дата последней транзакции.
 * Вынесена в чистую функцию, чтобы не тянуть корутины в тест (см. `AnchorModelTest`).
 */
internal fun defaultDaysAnchor(
    count: Int,
    firstTransactionDate: LocalDate,
    lastTransactionDate: LocalDate,
): LocalDate = if (count % 7 == 0) {
    firstTransactionDate.previousOrSame(DayOfWeek.MONDAY)
} else {
    lastTransactionDate
}
