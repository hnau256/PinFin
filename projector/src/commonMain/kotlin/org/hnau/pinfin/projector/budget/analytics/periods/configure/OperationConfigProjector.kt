package org.hnau.pinfin.projector.budget.analytics.periods.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arrow.core.toNonEmptyListOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.OperationConfigModel
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.OperationConfigModelState
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.fold
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.map
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.tab
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.budget.analytics.periods.configure.period.DurationProjector

/** Сумма / среднее (docs/analytics-v2-plan.md, "2.3", "2.6") - порт `ConfigOperationProjector`. */
class OperationConfigProjector(
    scope: CoroutineScope,
    private val model: OperationConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun duration(): DurationProjector.Dependencies
    }

    private val state: StateFlow<OperationConfigModelState<DurationProjector>> =
        model.state.mapState(scope) { state ->
            state.map { durationModel ->
                DurationProjector(
                    model = durationModel,
                    dependencies = dependencies.duration(),
                )
            }
        }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            Text(
                text = dependencies.localization.operation,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            val selectedTab by model.tab.collectAsState()
            STabs(
                items = remember {
                    OperationConfigModel.Tab.entries.toList().toNonEmptyListOrThrow()
                },
                getSelection = { selectedTab },
                onSelectionChanged = model.tab::value::set,
            ) { tab ->
                SText(
                    tab.fold(
                        ifSum = { dependencies.localization.sum },
                        ifAverage = { dependencies.localization.average },
                    )
                )
            }
            state
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    label = "OperationSelectedTab",
                    contentKey = { state -> state.tab.ordinal },
                    transitionSpec = TransitionSpec.remember(
                        showAlignment = Alignment.BottomCenter,
                    ),
                ) { state ->
                    state.fold(
                        ifSum = {},
                        ifAverage = { subperiod ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Dimens.separation),
                            ) {
                                Text(
                                    text = dependencies.localization.subperiod,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                subperiod.Content(
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    )
                }
        }
    }
}
