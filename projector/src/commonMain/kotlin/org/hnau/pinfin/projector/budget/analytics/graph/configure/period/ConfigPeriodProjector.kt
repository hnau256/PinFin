package org.hnau.pinfin.projector.budget.analytics.graph.configure.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.ConfigPeriodModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.PeriodPart
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.PeriodParts
import org.hnau.pinfin.projector.Localization

class ConfigPeriodProjector(
    model: ConfigPeriodModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    private val parts: PeriodParts<NonNegativeCountProjector> = model.parts.map { part, count ->
        NonNegativeCountProjector(
            model = count,
            title = when (part) {
                PeriodPart.Years -> dependencies.localization.years
                PeriodPart.Months -> dependencies.localization.month
                PeriodPart.Days -> dependencies.localization.days
            }
        )
    }

    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
            modifier = modifier,
        ) {
            PeriodPart
                .entries
                .forEach { part ->
                    parts[part].Content(
                        modifier = Modifier.weight(1f),
                    )
                }
        }
    }
}