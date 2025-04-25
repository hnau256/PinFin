package hnau.pinfin.repository.dto.sync

interface SyncApi {

    suspend fun <O, I : SyncHandle<O, I>> handle(
        request: I,
    ): O

    companion object
}