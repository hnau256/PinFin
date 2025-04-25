@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.manage

import arrow.core.identity
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.preferences.Preference
import hnau.common.app.preferences.Preferences
import hnau.common.app.preferences.map
import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.nullable
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.takeIf
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.shrinkType
import hnau.common.kotlin.toAccessor
import hnau.pinfin.upchain.BudgetId
import hnau.pinfin.model.LoadBudgetModel
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.repository.BudgetRepository
import hnau.pinfin.upchain.BudgetsStorage
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

        val preferences: Preferences

        val budgetsStorage: BudgetsStorage

        fun budgetsList(
            deferredBudgetRepositories: StateFlow<Map<BudgetId, Deferred<BudgetRepository>>>,
            budgetOpener: BudgetOpener,
        ): BudgetsListModel.Dependencies

        fun budget(
            deferredBudgetRepository: Deferred<BudgetRepository>,
            budgetsListOpener: BudgetsListOpener,
        ): LoadBudgetModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var stateSkeleton: ManageStateModel.Skeleton? = null,
    )

    private val selectedBudgetPreference: Preference<BudgetId?> = dependencies
        .preferences["selected_budget"]
        .map(
            scope = scope,
            mapper = Mapper.takeIf(
                predicate = String::isNotEmpty,
                restore = { "" }
            ) + BudgetId.stringMapper.nullable,
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
                extractKey = { (id) -> id },
                transform = { deferredScope, (id, budgetUpchain) ->
                    id to deferredScope.async {
                        BudgetRepository.create(
                            scope = deferredScope,
                            budgetUpchain = budgetUpchain,
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
        b = selectedBudgetPreference.value,
    ) { deferredBudgetRepositories, selectedOrNone ->
        selectedOrNone.getOrNull()?.let { selectedId ->
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
                        budgetOpener = selectedBudgetPreference.update
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
                )
            )

            else -> ManageStateModel.LoadBudget(
                model = LoadBudgetModel(
                    scope = stateScope,
                    dependencies = dependencies.budget(
                        deferredBudgetRepository = deferredBudgetRepositoryOrNull.deferredBudgetRepository,
                        budgetsListOpener = { selectedBudgetPreference.update(null) },
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