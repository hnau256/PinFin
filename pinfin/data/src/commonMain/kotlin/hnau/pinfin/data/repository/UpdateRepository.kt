package hnau.pinfin.data.repository

import hnau.pinfin.data.dto.Update

interface UpdateRepository {

    suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R

    suspend fun addUpdate(
        update: Update,
    )
}


