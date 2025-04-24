@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.manage

import arrow.core.identity
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.shrinkType
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.dto.BudgetId
import hnau.pinfin.data.repository.BudgetRepository
import hnau.pinfin.data.storage.BudgetStorage
import hnau.pinfin.data.storage.BudgetsStorage
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.LoadBudgetModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ManageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        fun budgetsList(
            deferredBudgetRepositories: StateFlow<Map<BudgetId, Deferred<BudgetRepository>>>,
        ): BudgetsListModel.Dependencies

        fun budget(
            deferredBudgetRepository: Deferred<BudgetRepository>,
        ): LoadBudgetModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selectedBudget: MutableStateFlow<BudgetId?> = null.toMutableStateFlowAsInitial(),
        var stateSkeleton: ManageStateModel.Skeleton? = null,
    )

    private data class DeferredBudgetRepositoryWrapper(
        val id: BudgetId,
        val deferredBudgetRepository: Deferred<BudgetRepository>,
    ) {

        override fun equals(
            other: Any?,
        ): Boolean = other
            ?.castOrNull<DeferredBudgetRepositoryWrapper>()
            ?.id
            ?.id
            ?.takeIf { it == id.id } != null

        override fun hashCode(): Int =
            id.id.hashCode()
    }

    private val deferredBudgetRepositories: StateFlow<Map<BudgetId, Deferred<BudgetRepository>>> =
        dependencies
            .budgetsStorage
            .list
            .mapListReusable(
                scope = scope,
                extractKey = BudgetStorage::id,
                transform = { deferredScope, budgetStorage ->
                    budgetStorage.id to deferredScope.async {
                        BudgetRepository.create(
                            scope = deferredScope,
                            budgetStorage = budgetStorage,
                        )
                    }
                }
            )
            .mapState(scope) { deferredRepositoriesList ->
                deferredRepositoriesList.associate(::identity)
            }

    val state: StateFlow<ManageStateModel> = combineState(
        scope = scope,
        a = deferredBudgetRepositories,
        b = skeleton.selectedBudget,
    ) { deferredBudgetRepositories, selectedOrNull ->
        selectedOrNull
            ?.let { selectedId ->
                deferredBudgetRepositories[selectedId]?.let { deferredBudgetRepository ->
                    DeferredBudgetRepositoryWrapper(
                        id = selectedId,
                        deferredBudgetRepository = deferredBudgetRepository,
                    )
                }
            }
    }.mapWithScope(
        scope = scope,
    ) { stateScope, deferredBudgetRepositoryOrNull ->
        when (deferredBudgetRepositoryOrNull) {
            null -> ManageStateModel.BudgetsList(
                model = BudgetsListModel(
                    scope = stateScope,
                    dependencies = dependencies.budgetsList(
                        deferredBudgetRepositories = deferredBudgetRepositories,
                    ),
                    skeleton = skeleton::stateSkeleton
                        .toAccessor()
                        .shrinkType<_, ManageStateModel.Skeleton.BudgetsList>()
                        .getOrInit {
                            ManageStateModel.Skeleton.BudgetsList(
                                BudgetsListModel.Skeleton()
                            )
                        }
                        .skeleton,
                    onBudgetClick = { skeleton.selectedBudget.value = it },
                    onAddBudgetClick = dependencies.budgetsStorage::createNewBudget,
                )
            )

            else -> ManageStateModel.LoadBudget(
                model = LoadBudgetModel(
                    scope = stateScope,
                    dependencies = dependencies.budget(
                        deferredBudgetRepository = deferredBudgetRepositoryOrNull.deferredBudgetRepository
                    ),
                    skeleton = skeleton::stateSkeleton
                        .toAccessor()
                        .shrinkType<_, ManageStateModel.Skeleton.LoadBudget>()
                        .getOrInit {
                            ManageStateModel.Skeleton.LoadBudget(
                                LoadBudgetModel.Skeleton()
                            )
                        }
                        .skeleton,
                )
            )
        }
    }

    override val goBackHandler: GoBackHandler = state
        .flatMapState(scope, GoBackHandlerProvider::goBackHandler)
}