@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetstack

import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.app.model.stack.tryDropLast
import hnau.pinfin.model.AccountModel
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pipe.annotations.Pipe
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

    @Pipe
    interface Dependencies {

        fun account(): AccountModel.Dependencies

        @Pipe
        interface WithOpeners {

            fun budget(): BudgetModel.Dependencies

            fun transaction(): TransactionModel.Dependencies
        }

        fun withOpener(
            opener: BudgetStackOpener,
        ): WithOpeners

        val budgetRepository: BudgetRepository
    }

    private val dependenciesWithOpeners: Dependencies.WithOpeners = dependencies.withOpener(
        opener = BudgetStackOpenerImpl(
            stack = skeleton.stack,
        )
    )

    val stack: StateFlow<NonEmptyStack<BudgetStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            getKey = BudgetStackElementModel.Skeleton::key,
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
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        )

        is BudgetStackElementModel.Skeleton.Account -> BudgetStackElementModel.Account(
            AccountModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.account(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
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