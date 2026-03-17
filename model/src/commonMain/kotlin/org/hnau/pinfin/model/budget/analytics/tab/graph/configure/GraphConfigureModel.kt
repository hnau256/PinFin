package org.hnau.pinfin.model.budget.analytics.tab.graph.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split.ConfigSplitPeriodModel
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.transaction.utils.map
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig

class GraphConfigureModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    onReady: (AnalyticsConfig) -> Unit,
    onCancel: () -> Unit,
) {

    @Serializable
    data class Skeleton(
        val initial: AnalyticsConfig,
        val period: ConfigSplitPeriodModel.Skeleton = ConfigSplitPeriodModel.Skeleton.create(
            initial = initial.split.period,
        ),
    )

    val period = ConfigSplitPeriodModel(
        scope = scope,
        skeleton = skeleton.period,
    )

    private val editableConfig: StateFlow<Editable<AnalyticsConfig>> = period
        .editablePeriod
        .mapState(scope) { editablePeriod ->
            editablePeriod.map { period ->
                skeleton.initial.copy(
                    split = skeleton.initial.split.copy(
                        period = period,
                    )
                )
            }
        }

    val save: StateFlow<(() -> Unit)?> = editableConfig.mapState(scope) { editableConfig ->
        when (editableConfig) {
            Editable.Incorrect -> null
            is Editable.Value -> {
                editableConfig.changed.foldBoolean(
                    ifTrue = { { onReady(editableConfig.value) } },
                    ifFalse = { onCancel }
                )
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}