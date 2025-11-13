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
        val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(BudgetStackElementModel.Skeleton.Budget())),
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

    private val dependenciesWithOpeners: Dependencies.WithOpeners = dependencies.withOpener(
        opener = BudgetStackOpenerImpl(
            stack = skeleton.stack,
        ),
        transactionsOpener = { filters ->
            skeleton.stack.push(
                BudgetStackElementModel.Skeleton.Transactions(
                    skeleton = TransactionsModel.Skeleton.create(
                        initialFilters = filters,
                    )
                )
            )
        }
    )

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<BudgetStackElementModel.Skeleton, BudgetStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = BudgetStackElementModel.Skeleton::key,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
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
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        )

        is BudgetStackElementModel.Skeleton.Transactions -> BudgetStackElementModel.Transactions(
            TransactionsModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependenciesWithOpeners.transactions(),
            )
        )

        is BudgetStackElementModel.Skeleton.Account -> BudgetStackElementModel.Account(
            AccountStackModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.account(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        )

        is BudgetStackElementModel.Skeleton.Categories -> BudgetStackElementModel.Categories(
            CategoriesModel(
                scope = modelScope,
                dependencies = dependenciesWithOpeners.categories(),
            )
        )

        is BudgetStackElementModel.Skeleton.Category -> BudgetStackElementModel.Category(
            CategoryStackModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.category(),
                onReady = { this@BudgetStackModel.skeleton.stack.tryDropLast() },
            )
        )
    }

    val stack: StateFlow<NonEmptyStack<BudgetStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = BudgetStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}