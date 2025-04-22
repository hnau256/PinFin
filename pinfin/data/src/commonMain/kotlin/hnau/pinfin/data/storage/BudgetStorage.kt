package hnau.pinfin.data.storage

import hnau.pinfin.data.dto.BudgetId
import hnau.pinfin.data.dto.Update

interface BudgetStorage {

    val id: BudgetId

    suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R

    suspend fun addUpdate(
        update: Update,
    )
}