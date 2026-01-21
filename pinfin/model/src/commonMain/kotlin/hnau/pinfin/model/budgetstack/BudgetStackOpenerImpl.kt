package hnau.pinfin.model.budgetstack

import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.push
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.coroutines.flow.MutableStateFlow

class BudgetStackOpenerImpl(
    private val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementSkeleton>>,
) : BudgetStackOpener {

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
        info: TransactionInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.transaction(
                transaction = TransactionModel.Skeleton.createForEdit(
                    transaction = info,
                )
            )
        )
    }

    override fun openConfigAccount(
        info: AccountInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.account(
                info = info,
            )
        )
    }

    override fun openCategories() {
        open(
            BudgetStackModel.ElementSkeleton.categories(Unit)
        )
    }

    override fun openCategory(
        info: CategoryInfo,
    ) {
        open(
            BudgetStackModel.ElementSkeleton.category(
                info = info,
            )
        )
    }
}