package hnau.pinfin.model.budgetstack

import hnau.pinfin.data.budget.TransactionInfo
import hnau.pinfin.data.dto.TransactionType

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )
}