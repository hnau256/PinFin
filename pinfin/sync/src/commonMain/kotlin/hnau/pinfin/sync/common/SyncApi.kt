package hnau.pinfin.sync.common

interface SyncApi {

    suspend fun <O, I: SyncHandle<O>> handle(
        request: I,
    ): ApiResponse<O>
}