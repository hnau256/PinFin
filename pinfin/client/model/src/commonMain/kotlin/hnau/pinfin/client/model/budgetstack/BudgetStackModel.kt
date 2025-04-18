@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.budgetstack

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.app.model.stack.tryDropLast
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.model.budget.BudgetModel
import hnau.pinfin.client.model.transaction.TransactionModel
import hnau.pinfin.scheme.Transaction
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(BudgetStackElementModel.Skeleton.Budget())),
    )

    @Shuffle
    interface Dependencies {

        @Shuffle
        interface WithOpeners {

            fun budget(): BudgetModel.Dependencies

            fun transaction(): TransactionModel.Dependencies
        }

        fun withOpeners(
            editTransactionOpener: EditTransactionOpener,
            newTransactionOpener: NewTransactionOpener,
        ): WithOpeners

        val budgetRepository: BudgetRepository
    }

    private val dependenciesWithOpeners: Dependencies.WithOpeners = dependencies.withOpeners(
        editTransactionOpener = ::openTransaction,
        newTransactionOpener = { openTransaction(null) },
    )

    val budgetRepository: BudgetRepository
        get() = dependencies.budgetRepository

    val stack: StateFlow<NonEmptyStack<BudgetStackElementModel>> = run {
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

    private fun openTransaction(
        id: Transaction.Id?,
    ) {
        this@BudgetStackModel.skeleton.stack.push(
            BudgetStackElementModel.Skeleton.Transaction(
                skeleton = TransactionModel.Skeleton(
                    id = id,
                ),
            )
        )
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: BudgetStackElementModel.Skeleton,
    ): BudgetStackElementModel = when (skeleton) {
        is BudgetStackElementModel.Skeleton.Budget -> BudgetStackElementModel.Budget(
            BudgetModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependenciesWithOpeners.budget(),
            )
        )

        is BudgetStackElementModel.Skeleton.Transaction -> BudgetStackElementModel.Transaction(
            TransactionModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependenciesWithOpeners.transaction(),
                completed = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
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