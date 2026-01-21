@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.budget

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapWithScope
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncClientLoadBudgetModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val goBack: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        fun budget(
            id: BudgetId,
            repository: BudgetRepository,
        ): SyncClientBudgetModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val id: BudgetId,
        val isStopSyncDialogVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val isStopSyncDialogVisible: MutableStateFlow<Boolean>
        get() = skeleton.isStopSyncDialogVisible

    val state: StateFlow<Loadable<SyncClientBudgetModel>> = LoadableStateFlow(scope) {
        val repository = dependencies.budgetsStorage
        val id = skeleton.id
        repository.createNewBudgetIfNotExists(id)
        repository
            .list
            .mapNotNull {
                it
                    .firstOrNull { it.first == id }
                    ?.second
            }
            .first()
    }.mapWithScope(scope) { scope, repositoryOrLoading ->
        repositoryOrLoading.map { repository ->
            SyncClientBudgetModel(
                scope = scope,
                dependencies = dependencies.budget(
                    id = skeleton.id,
                    repository = repository,
                ),
                goBack = goBack,
            )
        }
    }

    fun stopSyncConfirm() {
        goBack()
    }

    fun stopSyncCancel() {
        skeleton
            .isStopSyncDialogVisible
            .value = false
    }

    private fun switchIsStopSyncDialogVisibility() {
        skeleton.isStopSyncDialogVisible.update(Boolean::not)
    }


    val goBackHandler: GoBackHandler = state
        .flatMapWithScope(scope) { scope, budgetOrLoading ->
            when (budgetOrLoading) {
                Loading -> ::switchIsStopSyncDialogVisibility.toMutableStateFlowAsInitial()
                is Ready -> budgetOrLoading
                    .value
                    .goBackHandler
                    .mapWithScope(scope) { scope, goBackOrNull ->
                        goBackOrNull ?: ::switchIsStopSyncDialogVisibility
                    }
            }
        }
}