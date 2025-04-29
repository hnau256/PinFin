@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.list

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.utils.budget.repository.BudgetInfo
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncClientListItemModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {
        val sererBudgetPeekHeight: UpchainHash?
        val local: Deferred<BudgetInfo>
    }

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler = TODO()
}