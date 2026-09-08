package org.hnau.pinfin.model.budgetstack

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        id: Transaction.Id,
        info: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
    )

    fun openConfigAccount(
        id: AccountId,
        info: AccountInfo,
    )

    fun openSettings()

    fun openCreateBudget()

    fun openSwitchBudget()

    fun openCategories()

    fun openCategory(
        id: CategoryId,
        info: CategoryInfo,
    )
}