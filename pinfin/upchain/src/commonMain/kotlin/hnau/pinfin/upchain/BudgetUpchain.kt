package hnau.pinfin.upchain

import hnau.pinfin.data.Update

interface BudgetUpchain {

    suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R

    suspend fun addUpdate(
        update: Update,
    )
}