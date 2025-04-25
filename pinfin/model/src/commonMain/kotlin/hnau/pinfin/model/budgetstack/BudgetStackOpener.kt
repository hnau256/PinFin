package hnau.pinfin.model.budgetstack

import hnau.pinfin.repository.TransactionInfo
import hnau.pinfin.repository.dto.TransactionType

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )
}