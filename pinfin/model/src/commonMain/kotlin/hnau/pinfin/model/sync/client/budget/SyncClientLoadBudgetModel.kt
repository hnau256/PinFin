@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.budget

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.shuffler.annotations.Shuffle
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
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository

        fun budget(
            id: BudgetId,
            repository: BudgetRepository,
        ): SyncClientBudgetModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val id: BudgetId,
        var state: SyncClientBudgetModel.Skeleton? = null,
        val isStopSyncDialogVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val isStopSyncDialogVisible: MutableStateFlow<Boolean>
        get() = skeleton.isStopSyncDialogVisible

    val state: StateFlow<Loadable<SyncClientBudgetModel>> = LoadableStateFlow(scope) {
        val repository = dependencies.budgetsRepository
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
            .await()
    }.mapWithScope(scope) { repositoryScope, repositoryOrLoading ->
        repositoryOrLoading.map { repository ->
            SyncClientBudgetModel(
                scope = repositoryScope,
                dependencies = dependencies.budget(
                    id = skeleton.id,
                    repository = repository,
                ),
                skeleton = skeleton::state
                    .toAccessor()
                    .getOrInit { SyncClientBudgetModel.Skeleton() },
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

    private val fallbackGoBackHandler: GoBackHandler =
        { skeleton.isStopSyncDialogVisible.update(Boolean::not) }.toMutableStateFlowAsInitial()

    override val goBackHandler: GoBackHandler = state
        .scopedInState(scope)
        .flatMapState(scope) { (budgetScope, budgetOrLoading) ->
            when (budgetOrLoading) {
                Loading -> fallbackGoBackHandler
                is Ready -> budgetOrLoading.value.goBackHandler.fallback(
                    scope = budgetScope,
                    fallback = fallbackGoBackHandler
                )
            }
        }
}