@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.server

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.sync.server.utils.ServerSyncApi
import hnau.pinfin.model.sync.server.utils.tcpSyncServer
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.net.InetAddress
import java.net.NetworkInterface

class SyncServerModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val goBack: () -> Unit,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        fun serverSyncApi(): ServerSyncApi.Dependencies
    }

    @Serializable
    data class Skeleton(
        val port: ServerPort,
        val stopServerDialogIsOpened: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    init {
        scope.launch {
            tcpSyncServer(
                port = skeleton.port,
                api = ServerSyncApi(
                    scope = scope,
                    dependencies = dependencies.serverSyncApi(),
                ),
                onThrowable = {/*TODO*/ },
            )
        }
    }

    val addresses: List<String> = NetworkInterface
        .getNetworkInterfaces()
        .asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterNotNull()
        .map(InetAddress::getHostAddress)
        .mapNotNull { it.takeIf(String::isNotEmpty) }
        .toList()
        .sortedBy(String::length)

    val stopServerDialogIsOpened: StateFlow<Boolean>
        get() = skeleton.stopServerDialogIsOpened

    fun stopServer() {
        skeleton.stopServerDialogIsOpened.value = true
    }

    fun confirmStopServerDialog() {
        goBack()
    }

    fun cancelStopServerDialog() {
        skeleton.stopServerDialogIsOpened.value = false
    }

    override val goBackHandler: GoBackHandler = {
        skeleton.stopServerDialogIsOpened.update { !it }
    }.toMutableStateFlowAsInitial()
}