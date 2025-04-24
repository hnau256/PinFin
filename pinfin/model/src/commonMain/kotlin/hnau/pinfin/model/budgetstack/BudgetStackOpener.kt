package hnau.pinfin.model.budgetstack

import hnau.pinfin.data.repository.TransactionInfo
import hnau.pinfin.data.dto.TransactionType

interface BudgetStackOpener {

    fun openNewTransaction(
        transactionType: TransactionType,
    )

    fun openEditTransaction(
        info: TransactionInfo,
    )
}