package hnau.pinfin.repository.dto.sync

import kotlinx.serialization.json.Json

class SyncApiForClient(
    private val send: suspend (String) -> String,
) : SyncApi {

    override suspend fun <O, I : SyncHandle<O, I>> handle(
        request: I,
    ): O {
        val requestJson = json.encodeToString(request.requestSerializer, request)
        val responseJson = send(requestJson)
        val response = json.decodeFromString(request.responseSerializer, responseJson)
        return response
    }

    companion object {

        private val json: Json = Json
    }
}