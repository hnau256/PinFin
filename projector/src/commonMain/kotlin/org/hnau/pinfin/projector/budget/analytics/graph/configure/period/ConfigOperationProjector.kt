package org.hnau.pinfin.projector.budget.analytics.graph.configure.period

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.Tabs
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.uikit.transition.SlideOrientation
import org.hnau.commons.app.projector.uikit.transition.getTransitionSpecForSlideByCompare
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation.ConfigOperationModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation.ConfigOperationModelState
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation.fold
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation.tab
import org.hnau.pinfin.projector.Localization

class ConfigOperationProjector(
    scope: CoroutineScope,
    private val model: ConfigOperationModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun configPeriod(): ConfigPeriodProjector.Dependencies
    }

    private val state: StateFlow<ConfigOperationModelState<ConfigPeriodProjector>> =
        model.state.mapState(scope) { state ->
            state.map { periodModel ->
                ConfigPeriodProjector(
                    model = periodModel,
                    dependencies = dependencies.configPeriod(),
                )
            }
        }

    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Tabs(
                items = remember { ConfigOperationModel.Tab.entries.toImmutableList() },
                selected = model.tab.collectAsState().value,
                onSelectedChanged = model.tab::value::set,
            ) { tab ->
                Text(
                    when (tab) {
                        ConfigOperationModel.Tab.Sum ->
                            dependencies.localization.sum

                        ConfigOperationModel.Tab.Average ->
                            dependencies.localization.average
                    }
                )
            }
            state
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = "OperationSelectedTab",
                    contentKey = { state ->
                        state.tab.ordinal
                    },
                    transitionSpec = TransitionSpec.vertical(),
                ) { state ->
                    state.fold(
                        ifSum = {},
                        ifAverage = { configPeriod ->
                            configPeriod.Content(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Dimens.separation),
                            )
                        }
                    )
                }
        }
    }
}