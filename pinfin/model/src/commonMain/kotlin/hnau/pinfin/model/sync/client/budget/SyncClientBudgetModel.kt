@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.budget

import arrow.core.flatMap
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.budget.utils.syncWithRemote
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.UseSerializers

class SyncClientBudgetModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    goBack: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val budgetRepository: BudgetRepository

        val tcpSyncClient: TcpSyncClient
    }


    private val synchronizationAttempt: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    private suspend fun doSynchronization(): Boolean = runCatching {
        withContext(Dispatchers.IO) {
            val upchainStorage = dependencies.budgetRepository.upchainStorage
            val upchainToUpdate = upchainStorage.upchain.value
            upchainToUpdate
                .syncWithRemote(
                    budgetId = dependencies.id,
                    remote = dependencies.tcpSyncClient,
                )
                .map { synchronizedUpchain ->
                    upchainStorage
                        .setNewUpchain(
                            currentUpchainToCheck = upchainToUpdate,
                            newUpchain = synchronizedUpchain,
                        )
                }
        }
    }
        .flatMap { it }
        .fold(
            onSuccess = { wasSaved ->
                if (!wasSaved) {
                    println("QWERTY: Was not saved")
                } else {
                    println("QWERTY: Saved")
                }
                wasSaved
            },
            onFailure = {
                println("QWERTY: Sync error: $it")
                false
            }
        )

    sealed interface Result {

        data class Success(
            val goBack: () -> Unit,
        ) : Result

        data class Error(
            val tryAgain: () -> Unit,
        ) : Result
    }

    val resultOrLoading: StateFlow<Loadable<Result>> = synchronizationAttempt
        .flatMapWithScope(scope) { scope, _ ->
            LoadableStateFlow(scope) {
                doSynchronization()
            }.mapState(scope) { resultOrLoading ->
                resultOrLoading.map { result ->
                    when (result) {
                        true -> Result.Success(
                            goBack = goBack,
                        )

                        false -> Result.Error(
                            tryAgain = { synchronizationAttempt.update(Int::inc) }
                        )
                    }
                }
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}