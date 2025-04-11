package hnau.pinfin.client.data

import hnau.pinfin.scheme.Update

interface UpdateRepository {

    suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R

    suspend fun addUpdate(
        update: Update,
    )
}


