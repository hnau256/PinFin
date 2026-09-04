package org.hnau.pinfin.projector.budget.analytics.periods.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.GroupByConfigModel
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.Label

/** Выбор группировки: категории / счета / только итог (docs/analytics-v2-plan.md, "2.3", "2.6"). */
class GroupByConfigProjector(
    private val model: GroupByConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            Text(
                text = dependencies.localization.groupBy,
                style = MaterialTheme.typography.labelLarge,
            )
            val selected = model.groupBy.collectAsState().value
            ChipsFlowRow(
                all = listOf(null) + AnalyticsConfig.GroupBy.entries,
            ) { item ->
                Label(
                    selected = item == selected,
                    onClick = { model.groupBy.value = item },
                ) {
                    Text(
                        item?.fold(
                            ifAccount = { dependencies.localization.groupByAccounts },
                            ifCategory = { dependencies.localization.groupByCategories },
                        ) ?: dependencies.localization.onlyTotal
                    )
                }
            }
        }
    }
}
