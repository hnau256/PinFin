package hnau.pinfin.model.sync.utils

interface SyncApi {

    suspend fun <O, I: SyncHandle<O>> handle(
        request: I,
    ): Result<O>
}