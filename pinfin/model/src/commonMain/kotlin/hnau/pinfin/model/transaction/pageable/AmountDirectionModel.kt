@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AmountDirectionModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    category: StateFlow<CategoryInfo?>, //TODO use
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val direction: MutableStateFlow<AmountDirection>,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                direction = AmountDirection.default.toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                direction: AmountDirection,
            ): Skeleton = Skeleton(
                direction = direction.toMutableStateFlowAsInitial(),
            )
        }
    }

    val direction: StateFlow<AmountDirection>
        get() = skeleton.direction

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}