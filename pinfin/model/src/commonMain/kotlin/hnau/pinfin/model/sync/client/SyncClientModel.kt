@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.app.goback.stateGoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.shrinkType
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncClientModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository

        @Shuffle
        interface WithSyncClient {

            fun list(
                budgetOpener: BudgetSyncOpener,
            ): SyncClientListModel.Dependencies

            fun budget(
                budgetRepository: Deferred<BudgetRepository>,
            ): SyncClientLoadBudgetModel.Dependencies
        }

        fun withSyncClient(
            tcpSyncClient: TcpSyncClient,
        ): WithSyncClient
    }

    private val dependenciesWithSyncClient: Dependencies.WithSyncClient =
        dependencies.withSyncClient(
            tcpSyncClient = TcpSyncClient(
                address = skeleton.address,
                port = skeleton.port,
            )
        )

    @Serializable
    data class Skeleton(
        val port: ServerPort,
        val address: ServerAddress,
        val selectedBudget: MutableStateFlow<BudgetId?> = null.toMutableStateFlowAsInitial(),
        var state: SyncClientStateModel.Skeleton? = null,
    )

    private val selectedRepository: StateFlow<Pair<BudgetId, Deferred<BudgetRepository>>?> =
        skeleton
            .selectedBudget
            .scopedInState(scope)
            .flatMapState(scope) { (selectedScope, selected) ->
                when (selected) {
                    null -> null.toMutableStateFlowAsInitial()
                    else -> dependencies.budgetsRepository.list.mapState(
                        scope = selectedScope,
                        transform = { it.firstOrNull { it.first == selected } },
                    )
                }
            }

    val state: StateFlow<SyncClientStateModel> =
        selectedRepository.mapWithScope(scope) { stateScope, idWithDeferredRepositoryOrNull ->
            when (idWithDeferredRepositoryOrNull) {
                null -> SyncClientStateModel.List(
                    SyncClientListModel(
                        scope = stateScope,
                        skeleton = skeleton::state
                            .toAccessor()
                            .shrinkType<_, SyncClientStateModel.Skeleton.List>()
                            .getOrInit { SyncClientStateModel.Skeleton.List(SyncClientListModel.Skeleton()) }
                            .skeleton,
                        dependencies = dependenciesWithSyncClient.list(
                            budgetOpener = { budgetId ->
                                skeleton.selectedBudget.value = budgetId
                            }
                        ),
                    )
                )

                else -> SyncClientStateModel.Budget(
                    SyncClientLoadBudgetModel(
                        scope = stateScope,
                        skeleton = skeleton::state
                            .toAccessor()
                            .shrinkType<_, SyncClientStateModel.Skeleton.Budget>()
                            .getOrInit {
                                SyncClientStateModel.Skeleton.Budget(
                                    SyncClientLoadBudgetModel.Skeleton()
                                )
                            }
                            .skeleton,
                        dependencies = run {
                            val (id, repository) = idWithDeferredRepositoryOrNull
                            dependenciesWithSyncClient.budget(
                                budgetRepository = repository,
                            )
                        },
                        goBack = ::clearSelectedBudget,
                    )
                )
            }
        }

    private fun clearSelectedBudget() {
        skeleton.selectedBudget.value = null
    }

    override val goBackHandler: GoBackHandler = state.stateGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = selectedRepository.mapState(scope) { selectedRepositoryOrNull ->
                selectedRepositoryOrNull?.let { ::clearSelectedBudget }
            },
        )
}