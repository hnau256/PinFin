package hnau.pinfin.data.sync

interface SyncApi {

    suspend fun <O, I : SyncHandle<O, I>> handle(
        request: I,
    ): O

    companion object
}