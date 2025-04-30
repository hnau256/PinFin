@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.BudgetSyncOpener
import hnau.pinfin.model.utils.budget.repository.BudgetInfo
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers

class SyncClientListItemModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    id: BudgetId,
    localOrServer: Ior<Deferred<BudgetInfo>, ServerBudgetPeekHash>,
) : GoBackHandlerProvider {

    @Shuffle
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

    val state: StateFlow<Loadable<State>> = run {
        val sync = { dependencies.budgetOpener.openBudgetToSync(id) }
        when (localOrServer) {
            is Ior.Right -> Loadable.Ready(
                State.Syncable(
                    sync = sync,
                    mode = State.Syncable.Mode.OnlyOnServer,
                )
            ).toMutableStateFlowAsInitial()

            is Ior.Both -> localOrServer
                .leftValue
                .toLoadableStateFlow(scope)
                .mapState(scope) { infoOrLoading ->
                    infoOrLoading.map { info ->
                        val localPeekHash = info.upchainStorage.upchain.value.peekHash
                        val serverPeekHash = localOrServer.rightValue.peekHash
                        when (localPeekHash) {
                            serverPeekHash -> State.Actual
                            else -> State.Syncable(
                                sync = sync,
                                mode = State.Syncable.Mode.Both,
                            )
                        }
                    }
                }

            is Ior.Left -> Loadable.Ready(
                State.Syncable(
                    sync = sync,
                    mode = State.Syncable.Mode.OnlyLocal,
                )
            ).toMutableStateFlowAsInitial()
        }
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}