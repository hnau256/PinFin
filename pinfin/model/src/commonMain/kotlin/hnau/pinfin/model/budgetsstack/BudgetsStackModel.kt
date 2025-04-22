@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetsstack

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.repository.BudgetsRepository
import hnau.pinfin.model.LoadBudgetModel
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetsStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(BudgetsStackElementModel.Skeleton.Budgets())),
    )

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository

        fun budgetsList(): BudgetsListModel.Dependencies

        fun loadBudget(): LoadBudgetModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<BudgetsStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            skeletonsStack = stack,
        ) { modelScope, skeleton ->
            createModel(
                modelScope = modelScope,
                skeleton = skeleton,
            )
        }
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: BudgetsStackElementModel.Skeleton,
    ): BudgetsStackElementModel = when (skeleton) {
        is BudgetsStackElementModel.Skeleton.Budgets -> BudgetsStackElementModel.Budgets(
            BudgetsListModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.budgetsList(),
                onBudgetClick = { id ->
                    this@BudgetsStackModel.skeleton.stack.push(
                        BudgetsStackElementModel.Skeleton.Budget(
                            skeleton = LoadBudgetModel.Skeleton(
                                id = id,
                            ),
                        )
                    )
                },
                onAddBudgetClick = dependencies.budgetsRepository::createNewBudget,
            )
        )

        is BudgetsStackElementModel.Skeleton.Budget -> BudgetsStackElementModel.Budget(
            LoadBudgetModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.loadBudget(),
            )
        )
    }

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}