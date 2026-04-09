@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.manage

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
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
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.castOrNull
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.filter
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.nullable
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.takeIf
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.IconModel
import org.hnau.pinfin.model.NoBudgetsModel
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

        fun budget(
            id: BudgetId,
            budgetRepository: BudgetRepository,
            budgetsListOpener: BudgetsListOpener,
        ): BudgetStackModel.Dependencies

        fun noBudgets(): NoBudgetsModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgetSkeleton: KeyValue<BudgetId, BudgetStackModel.Skeleton>? = null,
        var icon: IconModel.Skeleton? = null,
    )

    @SealUp(
        variants = [
            Variant(
                type = BudgetStackModel::class,
                identifier = "budgetStack",
            ),
            Variant(
                type = NoBudgetsModel::class,
                identifier = "noBudgets",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "ManageStateModel",
    )
    interface State {

        val goBackHandler: GoBackHandler

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

    private val budgetRepositories: StateFlow<NonEmptyList<KeyValue<BudgetId, BudgetRepository>>?> =
        dependencies
            .budgetsStorage
            .list
            .mapState(scope) { repositoriesList ->
                repositoriesList.toNonEmptyListOrNull()
            }

    private val selectedBudget: StateFlow<KeyValue<BudgetId, BudgetRepository>?> =
        budgetRepositories.flatMapWithScope(scope) { scope, repositoriesOrNull ->
            repositoriesOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { repositories ->
                    selectedBudgetPreference
                        .value
                        .mapState(scope) { selectedIdOrNull ->
                            val selectedById = selectedIdOrNull?.let { selectedId ->
                                repositories.find { it.key == selectedId }
                            }
                            selectedById ?: repositories.head
                        }
                }
            )
        }

    val state: StateFlow<ManageStateModel> = selectedBudget.mapWithScope(
        scope = scope,
    ) { scope, budgetIdWithRepositoryOrNull ->
        when (budgetIdWithRepositoryOrNull) {
            null -> State.noBudgets(
                scope = scope,
                dependencies = dependencies.noBudgets(),
            )

            else -> {
                val (budgetId, budgetRepository) = budgetIdWithRepositoryOrNull
                State.budgetStack(
                    scope = scope,
                    dependencies = dependencies.budget(
                        id = budgetId,
                        budgetRepository = budgetRepository,
                        budgetsListOpener = { selectedBudgetPreference.update(null) },
                    ),
                    skeleton = skeleton::budgetSkeleton
                        .toAccessor()
                        .filter { it.key == budgetId }
                        .getOrInit {
                            val skeleton = BudgetStackModel.Skeleton()
                            KeyValue(budgetId, skeleton)
                        }
                        .value,
                )
            }
        }
    }

    init {
        scope.launch {
            selectedBudget
                .collect { selectedBudgetOrNull ->
                    selectedBudgetPreference.update(selectedBudgetOrNull?.key)
                }
        }
    }

    val goBackHandler: GoBackHandler = state
        .flatMapState(scope, ManageStateModel::goBackHandler)
}