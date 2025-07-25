package hnau.pinfin.model.budgetstack

import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.push
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.AccountModel
import hnau.pinfin.model.CategoriesModel
import hnau.pinfin.model.CategoryModel
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.coroutines.flow.MutableStateFlow

class BudgetStackOpenerImpl(
    private val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementModel.Skeleton>>,
) : BudgetStackOpener {

    private fun open(
        skeleton: BudgetStackElementModel.Skeleton,
    ) {
        stack.push(skeleton)
    }

    override fun openNewTransaction(
        transactionType: TransactionType,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Transaction(
                skeleton = TransactionModel.Skeleton.createForNew(
                    transactionType = transactionType,
                )
            )
        )
    }

    override fun openEditTransaction(
        info: TransactionInfo,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Transaction(
                skeleton = TransactionModel.Skeleton.createForEdit(
                    info = info,
                )
            )
        )
    }

    override fun openConfigAccount(
        info: AccountInfo,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Account(
                skeleton = AccountModel.Skeleton(
                    info = info,
                )
            )
        )
    }

    override fun openCategories() {
        open(
            BudgetStackElementModel.Skeleton.Categories(
                skeleton = CategoriesModel.Skeleton()
            )
        )
    }

    override fun openCategory(
        info: CategoryInfo,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Category(
                skeleton = CategoryModel.Skeleton(
                    category = info,
                )
            )
        )
    }
}