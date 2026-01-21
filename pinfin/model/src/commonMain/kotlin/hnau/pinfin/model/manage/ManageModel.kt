@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.manage

import arrow.core.identity
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.preferences.Preference
import hnau.common.app.model.preferences.Preferences
import hnau.common.app.model.preferences.map
import hnau.common.app.model.preferences.withDefault
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.coroutines.flow.state.flatMapState
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mapWithScope
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
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
) {

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
    }

    @Serializable
    data class Skeleton(
        var stateSkeleton: ManageStateSkeleton? = null,
        var icon: IconModel.Skeleton? = null,
    )

    @SealUp(
        variants = [
            Variant(
                type = BudgetsStackModel::class,
                identifier = "budgetsStack",
            ),
            Variant(
                type = BudgetStackModel::class,
                identifier = "budgetStack",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "ManageStateModel",
    )
    interface State {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetsStackModel.Skeleton::class,
                identifier = "budgetsStack",
            ),
            Variant(
                type = BudgetStackModel.Skeleton::class,
                identifier = "budgetStack",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "ManageStateSkeleton",
        serializable = true,
    )
    interface StateSkeleton {

        companion object
    }

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
        .flatMapWithScope(scope) { scope, selectedOrNone ->
            val selected = selectedOrNone
            when (selectedOrNone) {
                null -> null.toMutableStateFlowAsInitial()
                else -> budgetRepositories.mapState(
                    scope = scope,
                    transform = { it[selected] },
                )
            }
        }
        .mapWithScope(
            scope = scope,
        ) { scope, budgetRepositoryOrNull ->
            when (budgetRepositoryOrNull) {
                null -> State.budgetsStack(
                    scope = scope,
                    dependencies = dependencies.budgetsStack(
                        budgetRepositories = budgetRepositories,
                        budgetOpener = selectedBudgetPreference.update
                    ),
                    skeleton = skeleton::stateSkeleton
                        .toAccessor()
                        .shrinkType<_, ManageStateSkeleton.BudgetsStack>()
                        .getOrInit {
                            StateSkeleton.budgetsStack()
                        }
                        .skeleton,
                )

                else -> State.budgetStack(
                    scope = scope,
                    dependencies = dependencies.budget(
                        budgetRepository = budgetRepositoryOrNull,
                        budgetsListOpener = { selectedBudgetPreference.update(null) },
                    ),
                    skeleton = skeleton::stateSkeleton
                        .toAccessor()
                        .shrinkType<_, ManageStateSkeleton.BudgetStack>()
                        .getOrInit {
                            StateSkeleton.budgetStack()
                        }
                        .skeleton,
                )
            }
        }

    val goBackHandler: GoBackHandler = state
        .flatMapState(scope, ManageStateModel::goBackHandler)
}