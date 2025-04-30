@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.budget

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncClientLoadBudgetModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
): GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository
    }

    @Serializable
    data class Skeleton(
        val budgetId: BudgetId,
        val showCancelSyncDialog: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial()
    )


    override val goBackHandler: GoBackHandler = TODO()
}