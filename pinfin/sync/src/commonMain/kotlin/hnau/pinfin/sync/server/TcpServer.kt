package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncConstants
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.SyncJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.ServerSocket

suspend fun tcpServer(
    port: ServerPort,
    api: SyncApi,
): Result<Nothing> = runCatching {
    coroutineScope {
        val serverSocket = ServerSocket(port.port)
        while (true) {
            launch(Dispatchers.IO) {
                serverSocket.accept().use { clientSocket ->
                    val requestBytes = clientSocket
                        .inputStream
                        .use { input -> input.readAllBytes() }
                    val responseBytes = api.handle(requestBytes).getOrThrow()
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

private suspend fun SyncApi.handle(
    request: ByteArray,
): Result<ByteArray> {
    val requestString = request.toString(SyncConstants.charset)
    val typedRequest = SyncJson.decodeFromString(
        SyncHandle.serializer,
        requestString,
    )
    return handleTyped(typedRequest).map { response ->
        response.toByteArray(SyncConstants.charset)
    }
}


private suspend fun <O, I : SyncHandle<O>> SyncApi.handleTyped(
    request: I,
): Result<String> = handle(request).map { typedResult ->
    SyncJson.encodeToString(
        request.responseSerializer,
        typedResult,
    )
}