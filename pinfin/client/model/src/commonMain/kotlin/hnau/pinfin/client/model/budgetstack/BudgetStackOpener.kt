package hnau.pinfin.client.model.budgetstack

import hnau.pinfin.client.data.budget.TransactionInfo
import hnau.pinfin.scheme.TransactionType

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )
}