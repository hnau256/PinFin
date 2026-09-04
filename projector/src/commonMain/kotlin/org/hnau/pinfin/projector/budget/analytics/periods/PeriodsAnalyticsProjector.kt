package org.hnau.pinfin.projector.budget.analytics.periods

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.budget.analytics.tab.periods.PeriodsAnalyticsModel
import org.hnau.pinfin.model.budget.analytics.tab.periods.fold
import org.hnau.pinfin.projector.budget.analytics.periods.configure.AnalyticsConfigureProjector

/**
 * Заменяет старый `GraphProjector` (см. docs/analytics-v2-plan.md, "2.6. Структура кода"):
 * переключает между просмотром ([PeriodsProjector]) и заглушкой настроек
 * ([AnalyticsConfigureProjector] - полноценный экран появится в фазе 3).
 */
class PeriodsAnalyticsProjector(
    scope: CoroutineScope,
    model: PeriodsAnalyticsModel,
    dependencies: Dependencies,
) {

    @SealUp(
        variants = [
            Variant(
                type = PeriodsProjector::class,
                identifier = "configured",
            ),
            Variant(
                type = AnalyticsConfigureProjector::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "PeriodsStateProjector",
    )
    interface State {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    @Pipe
    interface Dependencies {

        fun configured(): PeriodsProjector.Dependencies

        fun configure(): AnalyticsConfigureProjector.Dependencies
    }

    private val state: StateFlow<PeriodsStateProjector> = model
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifConfigured = { model ->
                    State.configured(
                        scope = scope,
                        model = model,
                        dependencies = dependencies.configured(),
                    )
                },
                ifConfigure = { configureModel ->
                    State.configure(
                        scope = scope,
                        model = configureModel,
                        dependencies = dependencies.configure(),
                    )
                },
            )
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "periodsConfiguredOrConfigure",
                contentKey = { it.ordinal },
                transitionSpec = TransitionSpec.rememberCrossfade(),
            ) { state ->
                state.Content(contentPadding)
            }
    }
}
