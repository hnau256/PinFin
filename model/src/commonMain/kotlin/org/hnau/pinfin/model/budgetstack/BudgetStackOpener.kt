package org.hnau.pinfin.model.budgetstack

import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )

    fun openConfigAccount(
        info: AccountInfo,
    )

    fun openCategories()

    fun openCategory(
        info: CategoryInfo,
    )
}