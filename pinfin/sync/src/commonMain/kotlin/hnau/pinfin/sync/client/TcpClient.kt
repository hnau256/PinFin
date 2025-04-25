package hnau.pinfin.sync.client

import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncConstants
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.SyncJson
import hnau.pinfin.sync.server.ServerAddress
import hnau.pinfin.sync.server.ServerPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

class TcpClient(
    private val address: ServerAddress,
    private val port: ServerPort,
): SyncApi {

    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): Result<O> = runCatching {
        val requestBytes = withContext(Dispatchers.Default) {
            SyncJson
                .encodeToString(SyncHandle.serializer, request)
                .toByteArray(SyncConstants.charset)
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
                socket.inputStream.use {input ->
                    input.readAllBytes()
                }
            }
        }
        val response = withContext(Dispatchers.Default) {
            val responseString = responseBytes.toString(SyncConstants.charset)
            SyncJson.decodeFromString(
                request.responseSerializer,
                responseString,
            )
        }
        response
    }
}