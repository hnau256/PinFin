@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.upchain.core.UpchainId
import org.hnau.upchain.sync.client.core.sync
import org.hnau.upchain.sync.client.http.HttpSyncClient

class BudgetSyncMainModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    val config: StateFlow<SyncConfig>,
    openConfig: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val budgetRepository: BudgetRepository
    }

    val sync = actionOrCancelIfExecuting(scope) {
        val config = config.value
        coroutineScope {
            val client = HttpSyncClient(
                scope = this,
                scheme = config.scheme,
                host = config.host,
            )
            dependencies
                .budgetRepository
                .upchainRepository
                .sync(
                    id = dependencies.id.id.let(::UpchainId),
                    api = client,
                )
        }
    }

    val openConfig: StateFlow<(() -> Unit)?> = sync.mapState(scope) { syncOrCancel ->
        when (syncOrCancel) {
            is ActionOrElse.Action -> openConfig
            is ActionOrElse.Else -> null
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}