@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.manage

import arrow.core.identity
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.preferences.Preference
import hnau.common.app.model.preferences.Preferences
import hnau.common.app.model.preferences.map
import hnau.common.app.model.preferences.withDefault
import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.nullable
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.takeIf
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.shrinkType
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.IconModel
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.pinfin.model.budgetstack.BudgetStackModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.uuid.ExperimentalUuidApi

class ManageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val preferences: Preferences

        val budgetsStorage: BudgetsStorage

        fun budgetsStack(
            budgetRepositories: StateFlow<Map<BudgetId, BudgetRepository>>,
            budgetOpener: BudgetOpener,
        ): BudgetsStackModel.Dependencies

        fun budget(
            budgetRepository: BudgetRepository,
            budgetsListOpener: BudgetsListOpener,
        ): BudgetStackModel.Dependencies

        fun icon(): IconModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var stateSkeleton: ManageStateModel.Skeleton? = null,
        var icon: IconModel.Skeleton? = null,
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
        .withDefault(
            scope = scope,
        ) { null }

    @OptIn(ExperimentalUuidApi::class)
    private data class DeferredBudgetRepositoryWrapper(
        val id: BudgetId,
        val budgetRepository: BudgetRepository,
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

    private val budgetRepositories: StateFlow<Map<BudgetId, BudgetRepository>> =
        dependencies
            .budgetsStorage
            .list
            .mapState(scope) { repositoriesListList ->
                repositoriesListList.associate(::identity)
            }

    val state: StateFlow<ManageStateModel> = selectedBudgetPreference
        .value
        .flatMapWithScope(scope) { selectedScope, selectedOrNone ->
            val selected = selectedOrNone
            when (selectedOrNone) {
                null -> null.toMutableStateFlowAsInitial()
                else -> budgetRepositories.mapState(
                    scope = selectedScope,
                    transform = { it[selected] },
                )
            }
        }
        .mapWithScope(
            scope = scope,
        ) { stateScope, budgetRepositoryOrNull ->
            when (budgetRepositoryOrNull) {
                null -> ManageStateModel.BudgetsStack(
                    model = BudgetsStackModel(
                        scope = stateScope,
                        dependencies = dependencies.budgetsStack(
                            budgetRepositories = budgetRepositories,
                            budgetOpener = selectedBudgetPreference.update
                        ),
                        skeleton = skeleton::stateSkeleton
                            .toAccessor()
                            .shrinkType<_, ManageStateModel.Skeleton.BudgetsStack>()
                            .getOrInit {
                                ManageStateModel.Skeleton.BudgetsStack(
                                    BudgetsStackModel.Skeleton()
                                )
                            }
                            .skeleton,
                    )
                )

                else -> ManageStateModel.BudgetStack(
                    model = BudgetStackModel(
                        scope = stateScope,
                        dependencies = dependencies.budget(
                            budgetRepository = budgetRepositoryOrNull,
                            budgetsListOpener = { selectedBudgetPreference.update(null) },
                        ),
                        skeleton = skeleton::stateSkeleton
                            .toAccessor()
                            .shrinkType<_, ManageStateModel.Skeleton.BudgetStack>()
                            .getOrInit {
                                ManageStateModel.Skeleton.BudgetStack(
                                    BudgetStackModel.Skeleton()
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