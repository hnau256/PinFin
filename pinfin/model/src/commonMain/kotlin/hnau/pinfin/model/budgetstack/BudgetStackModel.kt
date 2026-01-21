@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.tryDropLast
import hnau.common.app.model.stack.withModels
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.CategoriesModel
import hnau.pinfin.model.TransactionsModel
import hnau.pinfin.model.accountstack.AccountStackModel
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.budget.analytics.tab.graph.TransactionsOpener
import hnau.pinfin.model.categorystack.CategoryStackModel
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
    )

    val stack: StateFlow<NonEmptyStack<BudgetStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = BudgetStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}