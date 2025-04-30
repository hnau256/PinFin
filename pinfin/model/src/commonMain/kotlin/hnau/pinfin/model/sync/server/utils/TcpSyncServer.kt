package hnau.pinfin.model.sync.server.utils

import hnau.pinfin.model.sync.utils.ApiResponse
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncConstants
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.sync.utils.readSizeWithBytes
import hnau.pinfin.model.sync.utils.writeSizeWithBytes
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

suspend fun tcpSyncServer(
    port: ServerPort,
    api: SyncApi,
    onThrowable: (Throwable) -> Unit,
): Result<Nothing> = runCatching {
    withContext(Dispatchers.IO) {
        val serverSocket = aSocket(SelectorManager(Dispatchers.IO))
            .tcp()
            .bind(port = port.port)
        println("QWERTY: Server address: ${serverSocket.localAddress}")
        coroutineScope {
            while (true) {
                try {
                    circleUnsafe(
                        serverSocket = serverSocket,
                        api = api,
                    )
                } catch (th: Throwable) {
                    onThrowable(th)
                }
            }
        }
        awaitCancellation()
    }
}

private suspend fun CoroutineScope.circleUnsafe(
    serverSocket: ServerSocket,
    api: SyncApi,
) {
    val clientSocket = serverSocket.accept()
    launch {
        clientSocket.use { clientSocket ->
            val requestBytes = clientSocket
                .openReadChannel()
                .readSizeWithBytes()
            val responseBytes = api.handle(requestBytes)
            clientSocket
                .openWriteChannel()
                .writeSizeWithBytes(responseBytes)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun SyncApi.handle(
    request: ByteArray,
): ByteArray {
    val typedRequest = SyncConstants.cbor.decodeFromByteArray(
        SyncHandle.serializer,
        request,
    )
    return handleTyped(typedRequest)
}


private suspend fun <O, I : SyncHandle<O>> SyncApi.handleTyped(
    request: I,
): ByteArray = handle(request)
    .fold(
        onSuccess = { result -> ApiResponse.Success(result) },
        onFailure = { error -> ApiResponse.Error(error.message) }
    )
    .let { response ->
        ApiResponse
            .createByteArrayMapper(request.responseSerializer)
            .reverse(response)
    }