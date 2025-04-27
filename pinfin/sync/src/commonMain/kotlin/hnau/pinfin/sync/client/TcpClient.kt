package hnau.pinfin.sync.client

import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncConstants
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.server.dto.ServerAddress
import hnau.pinfin.sync.server.dto.ServerPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.Socket

class TcpClient(
    private val address: ServerAddress,
    private val port: ServerPort,
): SyncApi {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): ApiResponse<O> = try {
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
    } catch (th: Throwable) {
        ApiResponse.Error(
            message = th.message,
        )
    }
}