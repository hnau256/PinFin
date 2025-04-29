package hnau.pinfin.model.sync.client

import arrow.core.flatMap
import hnau.pinfin.model.sync.utils.ApiResponse
import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncConstants
import hnau.pinfin.model.sync.utils.SyncHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.Socket

class TcpClient(
    private val address: ServerAddress,
    private val port: ServerPort,
) : SyncApi {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): Result<O> = runCatching {
        val requestBytes = withContext(Dispatchers.Default) {
            SyncConstants.cbor.encodeToByteArray(SyncHandle.serializer, request)
        }
        val responseBytes = withContext(Dispatchers.IO) {
            Socket(
                address.address,
                port.port,
            ).use { socket ->
                socket.outputStream.use { output ->
                    output.write(requestBytes)
                    output.flush()
                }
                socket.inputStream.use { input ->
                    input.readAllBytes()
                }
            }
        }
        val response = withContext(Dispatchers.Default) {
            SyncConstants.cbor.decodeFromByteArray(
                ApiResponse.serializer(request.responseSerializer),
                responseBytes,
            )
        }
        response
    }.flatMap { response ->
        when (response) {
            is ApiResponse.Success ->
                Result.success(response.data)

            is ApiResponse.Error ->
                Result.failure(Exception("Error received from sync server: ${response.error}"))
        }
    }
}