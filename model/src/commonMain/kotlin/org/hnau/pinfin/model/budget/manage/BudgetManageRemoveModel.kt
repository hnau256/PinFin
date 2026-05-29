@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.manage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrInProgressIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class BudgetManageRemoveModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {


        val repository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val dialogIsVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    data class Dialog(
        val cancel: () -> Unit,
        val remove: StateFlow<ActionOrElse<Unit, CancelOrInProgress.InProgress>>,
    )

    fun onRemoveClick() {
        skeleton.dialogIsVisible.value = true
    }

    val dialog: StateFlow<Dialog?> = skeleton
        .dialogIsVisible
        .mapWithScope(
            scope = scope,
        ) { scope, dialogIsVisible ->
            if (!dialogIsVisible) {
                return@mapWithScope null
            }
            Dialog(
                cancel = { skeleton.dialogIsVisible.value = false },
                remove = actionOrInProgressIfExecuting(
                    scope = scope,
                ) {
                    dependencies
                        .repository
                        .remove()
                }
            )
        }

    val goBackHandler: GoBackHandler = dialog.mapState(
        scope = scope,
    ) { dialogOrNull -> dialogOrNull?.cancel }
}