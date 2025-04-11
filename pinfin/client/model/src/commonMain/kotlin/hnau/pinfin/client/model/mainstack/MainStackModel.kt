@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.mainstack

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
import hnau.pinfin.client.model.MainModel
import hnau.pinfin.client.model.transaction.TransactionModel
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.TransactionType
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class MainStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<MainStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(MainStackElementModel.Skeleton.Main())),
    )

    @Shuffle
    interface Dependencies {

        fun main(): MainModel.Dependencies

        fun transaction(): TransactionModel.Dependencies

        val budgetRepository: BudgetRepository
    }

    val budgetRepository: BudgetRepository
        get() = dependencies.budgetRepository

    val stack: StateFlow<NonEmptyStack<MainStackElementModel>> = run {
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
        toEdit: Pair<Transaction.Id, Transaction>?,
    ) {
        val skeleton = toEdit
            ?.let { (id, transaction) ->
                TransactionModel.Skeleton.createForEdit(
                    id = id,
                    transaction = transaction,
                )
            }
            ?: TransactionModel.Skeleton.createForNew(
                transactionType = TransactionType.Transfer,
            )
        this@MainStackModel.skeleton.stack.push(
            MainStackElementModel.Skeleton.Transaction(
                skeleton = skeleton,
            )
        )
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: MainStackElementModel.Skeleton,
    ): MainStackElementModel = when (skeleton) {
        is MainStackElementModel.Skeleton.Main -> MainStackElementModel.Main(
            MainModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.main(),
                onAddTransactionClick = {
                    openTransaction(
                        toEdit = null,
                    )
                },
                onEditTransactionClick = { id, transaction ->
                    openTransaction(
                        toEdit = id to transaction,
                    )
                }
            )
        )

        is MainStackElementModel.Skeleton.Transaction -> MainStackElementModel.Transaction(
            TransactionModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.transaction(),
                completed = { this@MainStackModel.skeleton.stack.tryDropLast() }
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