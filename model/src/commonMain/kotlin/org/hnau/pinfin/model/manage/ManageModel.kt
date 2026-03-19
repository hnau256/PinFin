@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.manage

import arrow.core.identity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.preferences.Preference
import org.hnau.commons.app.model.preferences.Preferences
import org.hnau.commons.app.model.preferences.map
import org.hnau.commons.app.model.preferences.withDefault
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.castOrNull
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.nullable
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.takeIf
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.shrinkType
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.IconModel
import org.hnau.pinfin.model.budgetsstack.BudgetsStackModel
import org.hnau.pinfin.model.budgetstack.BudgetStackModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
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

    private val selectedBudget: StateFlow<BudgetRepository?> = selectedBudgetPreference
        .value
        .flatMapWithScope(scope) { scope, selected ->
            when (selected) {
                null -> null.toMutableStateFlowAsInitial()
                else -> budgetRepositories.mapState(
                    scope = scope,
                    transform = { it[selected] },
                )
            }
        }

    val state: StateFlow<ManageStateModel> = selectedBudget.mapWithScope(
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

    init {
        scope.launch {
            selectedBudget
                .mapNotNull { selectedBudgetOrNull ->
                    selectedBudgetOrNull.foldNullable(
                        ifNull = { },
                        ifNotNull = { null }
                    )
                }
                .collect {
                    selectedBudgetPreference.update(null)
                }
        }
    }

    val goBackHandler: GoBackHandler = state
        .flatMapState(scope, ManageStateModel::goBackHandler)
}