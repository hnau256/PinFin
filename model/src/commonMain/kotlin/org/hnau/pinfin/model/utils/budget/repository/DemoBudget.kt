package org.hnau.pinfin.model.utils.budget.repository

import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
import org.hnau.upchain.core.Update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

object DemoBudget {

    val id: BudgetId = "bca3f6b1-3ffc-40a9-bd50-a42150b6abd4".let(BudgetId.stringMapper.direct)

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
            )
            addAll(generator.generate(start, end))
        }.map(UpdateType.updateTypeMapper.reverse)
}
