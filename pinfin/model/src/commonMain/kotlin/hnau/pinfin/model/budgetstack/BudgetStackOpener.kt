package hnau.pinfin.model.budgetstack

import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.data.TransactionType

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )
}