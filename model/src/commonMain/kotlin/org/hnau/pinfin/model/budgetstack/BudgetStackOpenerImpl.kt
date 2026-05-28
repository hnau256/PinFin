package org.hnau.pinfin.model.budgetstack

import kotlinx.coroutines.flow.MutableStateFlow
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.BudgetSettingsModel
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

class BudgetStackOpenerImpl(
    private val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementSkeleton>>,
    private val dependencies: Dependencies,
) : BudgetStackOpener {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    private fun open(
        skeleton: BudgetStackElementSkeleton,
    ) {
        stack.push(skeleton)
    }

    override fun openNewTransaction(
        transactionType: TransactionType,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.transaction(
                transaction = TransactionModel.Skeleton.createForNew(
                    type = transactionType,
                )
            )
        )
    }

    override fun openEditTransaction(
        id: Transaction.Id,
        info: TransactionInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.transaction(
                transaction = TransactionModel.Skeleton.createForEdit(
                    id = id,
                    transaction = info,
                )
            )
        )
    }

    override fun openConfigAccount(
        id: AccountId,
        info: AccountInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.account(
                id = id,
                info = info,
            )
        )
    }

    override fun openConfig() {
        open(
            BudgetStackModel.ElementSkeleton.config(
                BudgetSettingsModel.Skeleton.create(
                    info = dependencies
                        .budgetRepository
                        .state
                        .value
                        .info,
                )
            )
        )
    }

    override fun openCategories() {
        open(
            BudgetStackModel.ElementSkeleton.categories
        )
    }

    override fun openCategory(
        id: CategoryId,
        info: CategoryInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.category(
                id = id,
                info = info,
            )
        )
    }
}