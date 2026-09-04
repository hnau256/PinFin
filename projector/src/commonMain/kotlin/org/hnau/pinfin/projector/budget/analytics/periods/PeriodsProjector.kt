package org.hnau.pinfin.projector.budget.analytics.periods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Overcompose
import org.hnau.commons.app.projector.utils.copy
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.periods.PeriodsFlowModel
import org.hnau.pinfin.model.budget.analytics.tab.periods.PeriodsModel
import org.hnau.pinfin.model.budget.analytics.tab.periods.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.budget.analytics.periods.utils.formatPeriod
import org.hnau.pinfin.projector.budget.analytics.periods.utils.summary

/**
 * Заменяет старые `GraphConfigFlowProjector` + `GraphConfigProjector` + `GraphPagesProjector`
 * (см. docs/analytics-v2-plan.md, "2.6. Структура кода"): шапка с заголовком периода, стрелками
 * навигации и индикатором пересчёта, плюс содержимое текущего периода ([PeriodProjector]).
 */
class PeriodsProjector(
    scope: CoroutineScope,
    private val model: PeriodsFlowModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun period(): PeriodProjector.Dependencies
    }

    private val periods: StateFlow<PeriodsModel> = model.periods

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val periodsModel by periods.collectAsState()
        periodsModel
            .state
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
            ) { delayed ->
                delayed.value.fold(
                    ifEmpty = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(dependencies.localization.noTransactions)
                        }
                    },
                    ifData = { period, pageModel, switchToPrevious, switchToNext ->
                        Overcompose(
                            contentPadding = contentPadding,
                            modifier = Modifier.fillMaxSize(),
                            top = { topPadding ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(topPadding.copy(bottom = 0.dp))
                                            .horizontalDisplayPadding(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                                    ) {
                                        NavigateIcon(
                                            onClick = switchToPrevious,
                                            icon = Icons.Default.ChevronLeft,
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            text = periodsModel.config.period.formatPeriod(period),
                                        )
                                        NavigateIcon(
                                            onClick = switchToNext,
                                            icon = Icons.Default.ChevronRight,
                                        )
                                        IconButton(
                                            onClick = model.configure,
                                        ) {
                                            Icon(Icons.Default.Settings)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = model.configure)
                                            .horizontalDisplayPadding(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            text = periodsModel.config.summary(dependencies.localization),
                                        )
                                    }
                                    if (delayed.isInProgress) {
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            },
                        ) { innerPadding ->
                            PeriodProjector(
                                model = pageModel,
                                dependencies = dependencies.period(),
                            ).Content(
                                contentPadding = innerPadding,
                            )
                        }
                    },
                )
            }
    }

    @Composable
    private fun NavigateIcon(
        onClick: (() -> Unit)?,
        icon: ImageVector,
    ) {
        IconButton(
            onClick = { onClick?.invoke() },
            enabled = onClick != null,
        ) {
            Icon(icon = icon)
        }
    }
}
