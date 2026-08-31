package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf

class ConfigPeriodModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
) {

    @Serializable
    data class Skeleton(
        val parts: PeriodParts<NonNegativeCountModel.Skeleton>,
    ) {

        companion object {

            fun create(
                initial: DatePeriod,
            ): Skeleton = Skeleton(
                parts = PeriodParts(
                    years = initial.years,
                    months = initial.months,
                    days = initial.days,
                ).map { count ->
                    NonNegativeCountModel.Skeleton(
                        initial = count,
                    )
                }
            )
        }
    }

    val parts: PeriodParts<NonNegativeCountModel> = skeleton
        .parts
        .map { part ->
            NonNegativeCountModel(
                scope = scope,
                skeleton = part,
            )
        }

    internal val periodEditable: StateFlow<Editable<DatePeriod>> = derivedStateFlowOf(scope) {
        editable {
            DatePeriod(
                years = parts.years.countEditable.state.bind(),
                months = parts.months.countEditable.state.bind(),
                days = parts.days.countEditable.state.bind(),
            )
        }
    }
}