package hnau.pinfin.model.sync.client.budget

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.data.BudgetId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class SyncClientBudgetModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
): GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

    }

    @Serializable
    data class Skeleton(
        val budgetId: BudgetId,
    )

    override val goBackHandler: GoBackHandler = TODO()
}