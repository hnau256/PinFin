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
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.app.projector.utils.getTransitionSpecForSlide
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.ConfigSplitPeriodModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.ConfigSplitPeriodModelState
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.fold
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.tab
import org.hnau.pinfin.projector.Localization

class ConfigSplitPeriodProjector(
    scope: CoroutineScope,
    private val model: ConfigSplitPeriodModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun configPeriod(): ConfigPeriodProjector.Dependencies
    }

    private val state: StateFlow<ConfigSplitPeriodModelState<ConfigPeriodProjector>> =
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
                items = remember { ConfigSplitPeriodModel.Tab.entries.toImmutableList() },
                selected = model.tab.collectAsState().value,
                onSelectedChanged = model.tab::value::set,
            ) { tab ->
                Text(
                    when (tab) {
                        ConfigSplitPeriodModel.Tab.Inclusive ->
                            dependencies.localization.inclusivePeriod

                        ConfigSplitPeriodModel.Tab.Fixed ->
                            dependencies.localization.fixedPeriod
                    }
                )
            }
            state
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = "SplitPeriodSelectedTab",
                    contentKey = { state ->
                        state.tab.ordinal
                    },
                    transitionSpec = getTransitionSpecForSlide(
                        orientation = SlideOrientation.Horizontal,
                    ) {
                        when (targetState.tab.ordinal > initialState.tab.ordinal) {
                            true -> 1f
                            false -> -1f
                        } * 0.25f
                    }
                ) { state ->
                    state.fold(
                        ifInclusive = {},
                        ifFixed = { configPeriod ->
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