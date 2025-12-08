package hnau.pinfin.model.sync.client.utils

import arrow.core.flatMap
import hnau.pinfin.model.sync.utils.ApiResponse
import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncConstants
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.sync.utils.readSizeWithBytes
import hnau.pinfin.model.sync.utils.writeSizeWithBytes
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

class TcpSyncClient(
    private val address: ServerAddress,
    private val port: ServerPort,
) : SyncApi {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): Result<O> = runCatching {
        val requestBytes = withContext(Dispatchers.Default) {
            SyncConstants.cbor.encodeToByteArray(SyncHandle.Companion.serializer, request)
        }
        val responseBytes = withContext(Dispatchers.IO) {

            aSocket(SelectorManager(Dispatchers.IO))
                .tcp()
                .connect(
                    InetSocketAddress(
                        hostname = address.address,
                        port = port.port,
                    )
                )
                .use { socket ->
                    socket
                        .openWriteChannel()
                        .writeSizeWithBytes(requestBytes)
                    socket
                        .openReadChannel()
                        .readSizeWithBytes()
                }
        }
        val response = withContext(Dispatchers.Default) {
            ApiResponse
                .createByteArrayMapper(
                    dataSerializer = request.responseSerializer,
                )
                .direct(responseBytes)
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