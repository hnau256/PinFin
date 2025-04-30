@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client.budget

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

        val budgetRepository: Deferred<BudgetRepository>

        fun budget(
            repository: BudgetRepository,
        ): SyncClientBudgetModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var state: SyncClientBudgetModel.Skeleton? = null,
        val isStopSyncDialogVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<Loadable<SyncClientBudgetModel>> = dependencies
        .budgetRepository
        .toLoadableStateFlow(scope)
        .mapWithScope(scope) { repositoryScope, repositoryOrLoading ->
            repositoryOrLoading.map { repository ->
                SyncClientBudgetModel(
                    scope = repositoryScope,
                    dependencies = dependencies.budget(
                        repository = repository,
                    ),
                    skeleton = skeleton::state
                        .toAccessor()
                        .getOrInit { SyncClientBudgetModel.Skeleton() },
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

    override val goBackHandler: GoBackHandler = state.flatMapState(scope) { budgetOrLoading ->
        when (budgetOrLoading) {
            Loading -> {
                { skeleton.isStopSyncDialogVisible.update(Boolean::not) }.toMutableStateFlowAsInitial()
            }

            is Ready -> budgetOrLoading.value.goBackHandler
        }
    }
}