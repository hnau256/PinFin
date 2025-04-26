package hnau.pinfin.upchain

interface BudgetUpchain {

    suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R

    suspend fun addUpdates(
        updates: List<Update>,
    )
}