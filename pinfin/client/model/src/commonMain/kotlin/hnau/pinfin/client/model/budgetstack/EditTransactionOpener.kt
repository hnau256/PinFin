package hnau.pinfin.client.model.budgetstack

import hnau.pinfin.scheme.Transaction

fun interface EditTransactionOpener {

    fun openEditTransaction(
        id: Transaction.Id,
    )
}