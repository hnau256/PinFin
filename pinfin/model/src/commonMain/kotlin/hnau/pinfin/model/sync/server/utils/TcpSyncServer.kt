package hnau.pinfin.model.sync.server.utils

import hnau.pinfin.model.sync.utils.ApiResponse
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncConstants
import hnau.pinfin.model.sync.utils.SyncHandle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.InputStream
import java.net.ServerSocket

suspend fun tcpSyncServer(
    port: ServerPort,
    api: SyncApi,
    onThrowable: (Throwable) -> Unit,
): Result<Nothing> = withContext(Dispatchers.IO) {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, th ->
        onThrowable(th)
    }
    runCatching {
        coroutineScope {
            val serverSocket = ServerSocket(port.port)
            while (true) {
                yield()
                val clientSocket = serverSocket.accept()
                launch(Dispatchers.IO + coroutineExceptionHandler) {
                    clientSocket.use { clientSocket ->
                        val requestBytes = clientSocket
                            .inputStream
                            .use(InputStream::readAllBytes)
                        val responseBytes = api.handle(requestBytes)
                        clientSocket
                            .outputStream
                            .use { output ->
                                output.write(responseBytes)
                                output.flush()
                            }
                    }
                }
            }
        }
        awaitCancellation()
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


@OptIn(ExperimentalSerializationApi::class)
private suspend fun <O, I : SyncHandle<O>> SyncApi.handleTyped(
    request: I,
): ByteArray {
    val typedResult = handle(request).fold(
        onSuccess = {result -> ApiResponse.Success(result) },
        onFailure = { error -> ApiResponse.Error(error.message) }
    )
    return SyncConstants.cbor.encodeToByteArray(
        ApiResponse.serializer(request.responseSerializer),
        typedResult,
    )
}