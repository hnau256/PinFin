@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit

/**
 * Число + единица (день / месяц / год) — «Свой» период (docs/analytics-v2-plan.md, "2.6",
 * `PeriodConfigModel`), а также подпериод «Среднего» ([org.hnau.pinfin.model.budget.analytics.tab.periods.configure.OperationConfigModel]).
 * Аналог старого `ConfigPeriodModel`, но одна единица вместо Y/M/D одновременно
 * (см. "Решения автора", п. 7 — смешанные длительности не разрешаются).
 * Якорь не хранит — им занимается [AnchorModel].
 */
class DurationModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    @Serializable
    data class Skeleton(
        val initialUnit: PeriodUnit,
        val count: NonNegativeCountModel.Skeleton,
        val unit: MutableStateFlow<PeriodUnit> = initialUnit.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: PeriodDuration,
            ): Skeleton = Skeleton(
                initialUnit = initial.unit,
                count = NonNegativeCountModel.Skeleton(
                    initial = initial.count,
                ),
            )
        }
    }

    val count: NonNegativeCountModel = NonNegativeCountModel(
        scope = scope,
        skeleton = skeleton.count,
    )

    val unit: MutableStateFlow<PeriodUnit>
        get() = skeleton.unit

    /** Единица, всегда согласованная со значением count - используется там, где точность не критична (режим якоря). */
    val bestEffortCount: StateFlow<Int>
        get() = count.bestEffortCount

    val duration: StateFlow<Editable<PeriodDuration>> = derivedStateFlowOf(scope) {
        editable {
            PeriodDuration(
                count = count.countEditable.state.bind(),
                unit = unit.state,
            )
        }
    }
}
