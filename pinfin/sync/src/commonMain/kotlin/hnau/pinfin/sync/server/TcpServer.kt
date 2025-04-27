package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncConstants
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.server.dto.ServerPort
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.InputStream
import java.net.ServerSocket

suspend fun tcpServer(
    port: ServerPort,
    api: SyncApi,
    onThrowable: (Throwable) -> Unit,
): Result<Nothing> {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, th ->
        onThrowable(th)
    }
    return runCatching {
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
    val typedResult = handle(request)
    return SyncConstants.cbor.encodeToByteArray(
        ApiResponse.serializer(request.responseSerializer),
        typedResult,
    )
}