package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.transaction.utils.combineEditableWith

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

    internal val periodEditable: StateFlow<Editable<DatePeriod>> = parts
        .map(NonNegativeCountModel::countEditable)
        .let { parts ->
            parts
                .years
                .combineEditableWith(
                    scope = scope,
                    other = parts.months,
                ) { years, months ->
                    years to months
                }
                .combineEditableWith(
                    scope = scope,
                    other = parts.days,
                ) { (years, months), days ->
                    //TODO validate is not zero
                    DatePeriod(
                        years = years,
                        months = months,
                        days = days,
                    )
                }
        }
}