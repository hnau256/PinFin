@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig

/**
 * Выбор группировки (docs/analytics-v2-plan.md, "2.3", "2.6"): категории / счета / только итог
 * (`groupBy: GroupBy? = null`). Всегда корректен - отдельный `Editable` не нужен, но
 * `editableGroupBy` заведён для единообразия со сборкой [AnalyticsConfigureModel].
 */
class GroupByConfigModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    @Serializable
    data class Skeleton(
        val initial: AnalyticsConfig.GroupBy?,
        val groupBy: MutableStateFlow<AnalyticsConfig.GroupBy?> = initial.toMutableStateFlowAsInitial(),
    )

    val groupBy: MutableStateFlow<AnalyticsConfig.GroupBy?>
        get() = skeleton.groupBy

    val editableGroupBy: StateFlow<Editable<AnalyticsConfig.GroupBy?>> = skeleton
        .groupBy
        .mapState(scope) { groupBy ->
            Editable.Value(
                value = groupBy,
                changed = groupBy != skeleton.initial,
            )
        }
}
