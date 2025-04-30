package hnau.pinfin.model.sync.server.utils

import arrow.core.raise.result
import hnau.pinfin.model.sync.utils.ApiResponse
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncConstants
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.sync.utils.readSizeWithBytes
import hnau.pinfin.model.sync.utils.writeSizeWithBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

suspend fun tcpSyncServer(
    port: ServerPort,
    api: SyncApi,
    onThrowable: (Throwable) -> Unit,
): Result<Nothing> = result {
    val serverSocket = runCatching {
        withContext(Dispatchers.IO) {
            ServerSocket(port.port)
        }
    }.bind()
    try {
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
    } catch (ex: CancellationException) {
        runCatching { serverSocket.close() }.bind()
        throw ex
    }
}

private suspend fun CoroutineScope.circleUnsafe(
    serverSocket: ServerSocket,
    api: SyncApi,
) {
    withContext(Dispatchers.IO) {
        yield()
        val clientSocket = serverSocket.accept()
        launch(Dispatchers.IO) {
            clientSocket.use { clientSocket ->
                val requestBytes = clientSocket
                    .inputStream
                    .let(::DataInputStream)
                    .readSizeWithBytes()
                val responseBytes = api.handle(requestBytes)
                clientSocket
                    .outputStream
                    .let(::DataOutputStream)
                    .writeSizeWithBytes(responseBytes)
            }
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