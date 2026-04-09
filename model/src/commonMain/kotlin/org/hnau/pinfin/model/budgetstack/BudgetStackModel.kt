@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budgetstack

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.tryDropLast
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.CategoriesModel
import org.hnau.pinfin.model.TransactionsModel
import org.hnau.pinfin.model.accountstack.AccountStackModel
import org.hnau.pinfin.model.budget.BudgetModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.TransactionsOpener
import org.hnau.pinfin.model.categorystack.CategoryStackModel
import org.hnau.pinfin.model.sync.BudgetSyncStackModel
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class BudgetStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementSkeleton>> =
            MutableStateFlow(NonEmptyStack(ElementSkeleton.budget())),
    )

    @Pipe
    interface Dependencies {

        fun account(): AccountStackModel.Dependencies

        fun category(): CategoryStackModel.Dependencies

        fun sync(): BudgetSyncStackModel.Dependencies

        @Pipe
        interface WithOpeners {

            fun budget(): BudgetModel.Dependencies

            fun transaction(): TransactionModel.Dependencies

            fun transactions(): TransactionsModel.Dependencies

            fun categories(): CategoriesModel.Dependencies
        }

        fun withOpener(
            opener: BudgetStackOpener,
            transactionsOpener: TransactionsOpener,
        ): WithOpeners

        val budgetRepository: BudgetRepository
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetModel::class,
                identifier = "budget",
            ),
            Variant(
                type = TransactionModel::class,
                identifier = "transaction",
            ),
            Variant(
                type = TransactionsModel::class,
                identifier = "transactions",
            ),
            Variant(
                type = AccountStackModel::class,
                identifier = "account",
            ),
            Variant(
                type = CategoriesModel::class,
                identifier = "categories",
            ),
            Variant(
                type = CategoryStackModel::class,
                identifier = "category",
            ),
            Variant(
                type = BudgetSyncStackModel::class,
                identifier = "sync",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "BudgetStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetModel.Skeleton::class,
                identifier = "budget",
            ),
            Variant(
                type = TransactionModel.Skeleton::class,
                identifier = "transaction",
            ),
            Variant(
                type = TransactionsModel.Skeleton::class,
                identifier = "transactions",
            ),
            Variant(
                type = AccountStackModel.Skeleton::class,
                identifier = "account",
            ),
            Variant(
                type = Unit::class,
                identifier = "categories",
            ),
            Variant(
                type = CategoryStackModel.Skeleton::class,
                identifier = "category",
            ),
            Variant(
                type = BudgetSyncStackModel.Skeleton::class,
                identifier = "sync",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "BudgetStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val dependenciesWithOpeners: Dependencies.WithOpeners = dependencies.withOpener(
        opener = BudgetStackOpenerImpl(
            stack = skeleton.stack,
        ),
        transactionsOpener = { filters ->
            skeleton.stack.push(
                ElementSkeleton.transactions(
                    transactions = TransactionsModel.Skeleton.create(
                        initialFilters = filters,
                    )
                )
            )
        }
    )

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<BudgetStackElementSkeleton, BudgetStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = BudgetStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: BudgetStackElementSkeleton,
    ): BudgetStackElementModel = skeleton.fold(
        ifBudget = { budgetSkeleton ->
            Element.budget(
                scope = modelScope,
                skeleton = budgetSkeleton,
                dependencies = dependenciesWithOpeners.budget(),
            )
        },
        ifTransaction = { transactionSkeleton ->
            Element.transaction(
                scope = modelScope,
                skeleton = transactionSkeleton,
                dependencies = dependenciesWithOpeners.transaction(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        },
        ifTransactions = { transactionsSkeleton ->
            Element.transactions(
                scope = modelScope,
                skeleton = transactionsSkeleton,
                dependencies = dependenciesWithOpeners.transactions(),
            )
        },
        ifAccount = { accountSkeleton ->
            Element.account(
                scope = modelScope,
                skeleton = accountSkeleton,
                dependencies = dependencies.account(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        },
        ifCategories = {
            Element.categories(
                scope = modelScope,
                dependencies = dependenciesWithOpeners.categories(),
            )
        },
        ifCategory = { categorySkeleton ->
            Element.category(
                scope = modelScope,
                skeleton = categorySkeleton,
                dependencies = dependencies.category(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        },
        ifSync = { syncSkeleton ->
            Element.sync(
                scope = scope,
                skeleton = syncSkeleton,
                dependencies = dependencies.sync(),
            )
        }
    )

    val stack: StateFlow<NonEmptyStack<BudgetStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = BudgetStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}