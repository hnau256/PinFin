package hnau.pinfin.model.budgetstack

import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo

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
}