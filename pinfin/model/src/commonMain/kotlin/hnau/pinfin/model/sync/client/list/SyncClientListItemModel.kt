@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.BudgetSyncOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.pinfin.model.utils.toBudgetInfoStateFlow
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers

class SyncClientListItemModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    val id: BudgetId,
    localOrServer: Ior<BudgetRepository, SyncClientListModel.ServerBudget>,
) {

    @Pipe
    interface Dependencies {

        val budgetOpener: BudgetSyncOpener
    }

    sealed interface State {

        data object Actual : State

        data class Syncable(
            val sync: () -> Unit,
            val mode: Mode,
        ) : State {

            enum class Mode { OnlyOnServer, OnlyLocal, Both }
        }
    }

    val info: StateFlow<BudgetInfo> = when (localOrServer) {
        is Ior.Both ->
            localOrServer.leftValue.toBudgetInfoStateFlow(scope)

        is Ior.Left ->
            localOrServer.value.toBudgetInfoStateFlow(scope)

        is Ior.Right ->
            localOrServer.value.info.toMutableStateFlowAsInitial()
    }

    val state: State = run {
        val sync = { dependencies.budgetOpener.openBudgetToSync(id) }
        when (localOrServer) {
            is Ior.Right -> State.Syncable(
                sync = sync,
                mode = State.Syncable.Mode.OnlyOnServer,
            )


            is Ior.Both -> localOrServer
                .leftValue
                .let { repository ->
                    val localPeekHash = repository.upchainStorage.upchain.value.peekHash
                    val serverPeekHash = localOrServer.rightValue.peekHash
                    when (localPeekHash) {
                        serverPeekHash -> State.Actual
                        else -> State.Syncable(
                            sync = sync,
                            mode = State.Syncable.Mode.Both,
                        )
                    }
                }

            is Ior.Left -> State.Syncable(
                sync = sync,
                mode = State.Syncable.Mode.OnlyLocal,
            )
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}