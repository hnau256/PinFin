package org.hnau.pinfin.model.utils.budget.repository.demo

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.hnau.pinfin.data.UpdateType
import kotlin.time.Clock

object DemoBudget {

    val updates: List<UpdateType>
        get() = buildList {
            val end = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val start = end.minus(10 * 365, DateTimeUnit.DAY)
            val generator = DemoBudgetGenerator(
                config = DemoBudgetConfig(
                    currencyRate = 1.0,
                    annualInflation = 0.04,
                    taxRate = 0.13,
                    localization = DemoBudgetLocalization.ru,
                ),
                seed = 2L,
            )
            addAll(generator.generate(start, end))
        }
}
