package org.hnau.pinfin.model.utils.budget.repository.demo

import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
import org.hnau.upchain.core.Update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

object DemoBudget {

    val updates: List<Update>
        get() = buildList {
            val end = Clock.System.now()
            val start = end - (10 * 365).days
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
        }.map(UpdateType.updateTypeMapper.reverse)
}
