package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncHandle

interface SyncApi {

    suspend fun <O, I: SyncHandle<O>> handle(
        request: I,
    ): ApiResponse<O>
}