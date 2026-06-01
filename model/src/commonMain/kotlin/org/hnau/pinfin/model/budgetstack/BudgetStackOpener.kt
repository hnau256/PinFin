package org.hnau.pinfin.model.budgetstack

import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        id: Transaction.Id,
        info: TransactionInfo,
    )

    fun openConfigAccount(
        id: AccountId,
        info: AccountInfo,
    )

    fun openSettings()

    fun openCreateBudget()

    fun openCategories()

    fun openCategory(
        id: CategoryId,
        info: CategoryInfo,
    )
}