package org.hnau.pinfin.projector.budget.analytics.periods.configure.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period.DurationModel
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.analytics.period.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.Label

/**
 * Число + единица - "Свой" период, а также подпериод "Среднего" (docs/analytics-v2-plan.md,
 * "2.6", `DurationModel`). Одна единица за раз (см. "Решения автора", п. 7).
 */
class DurationProjector(
    private val model: DurationModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    private val count = NonNegativeCountProjector(
        model = model.count,
        title = {
            model.unit.collectAsState().value.fold(
                ifDay = { dependencies.localization.days },
                ifMonth = { dependencies.localization.months },
                ifYear = { dependencies.localization.years },
            )
        },
    )

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            count.Content(
                modifier = Modifier,
            )
            ChipsFlowRow(
                all = PeriodUnit.entries,
            ) { unit ->
                val selected = model.unit.collectAsState().value == unit
                Label(
                    selected = selected,
                    onClick = { model.unit.value = unit },
                ) {
                    Text(
                        unit.fold(
                            ifDay = { dependencies.localization.days },
                            ifMonth = { dependencies.localization.months },
                            ifYear = { dependencies.localization.years },
                        )
                    )
                }
            }
        }
    }
}
