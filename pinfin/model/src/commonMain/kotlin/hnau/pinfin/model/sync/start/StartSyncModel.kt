@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.start

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.preferences.Preference
import hnau.common.app.model.preferences.Preferences
import hnau.common.app.model.preferences.map
import hnau.common.app.model.preferences.withDefault
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.filterSet
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToInt
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.sync.SyncModeOpener
import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class StartSyncModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val preferences: Preferences

        val syncModeOpener: SyncModeOpener
    }

    private val serverAddressPreference: Preference<ServerAddress> = dependencies
        .preferences["sync_server_address"]
        .map(
            scope = scope,
            mapper = Mapper(::ServerAddress, ServerAddress::address),
        )
        .withDefault(
            scope = scope,
        ) { ServerAddress("") }

    val serverAddressPlaceholder: ServerAddress = serverAddressPreference
        .value
        .value

    private val portPreference: Preference<ServerPort> = dependencies
        .preferences["sync_port"]
        .map(
            scope = scope,
            mapper = Mapper.stringToInt + Mapper(::ServerPort, ServerPort::port),
        )
        .withDefault(
            scope = scope,
        ) { ServerPort.default }

    val portPlaceholder: ServerPort = portPreference
        .value
        .value

    private val inProgressRegistry = InProgressRegistry()

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    @Serializable
    data class Skeleton(
        val serverAddress: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),

        val port: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),
    )

    val portInput: MutableStateFlow<EditingString> =
        skeleton.port.filterSet { it.text.length <= ServerPort.maxLength && it.text.all { it.isDigit() } }

    private val port: StateFlow<ServerPort?> = skeleton.port.mapState(
        scope = scope,
    ) { portInput ->
        val inputString = portInput.text.trim().takeIf(String::isNotEmpty)
        when (inputString) {
            null -> portPlaceholder
            else -> ServerPort.tryParse(inputString)
        }
    }

    val portIsCorrect: StateFlow<Boolean> =
        port.mapState(scope) { it != null }

    val serverAddressInput: MutableStateFlow<EditingString>
        get() = skeleton.serverAddress

    private val serverAddress: StateFlow<ServerAddress?> = skeleton.serverAddress.mapState(
        scope = scope,
    ) { serverAddressInput ->
        val inputString = serverAddressInput.text.trim().takeIf(String::isNotEmpty)
        when (inputString) {
            null -> serverAddressPlaceholder
            else -> ServerAddress.tryParse(inputString)
        }
    }

    val serverAddressIsCorrect: StateFlow<Boolean> =
        serverAddress.mapState(scope) { it != null }

    val startServer: StateFlow<(() -> Unit)?> = port.mapState(scope) { portOrNull ->
        portOrNull?.let { port ->
            {
                startSyncServer(
                    port = port,
                )
            }
        }
    }

    private fun startSyncServer(
        port: ServerPort,
    ) {
        scope.launch {
            inProgressRegistry.executeRegistered {
                portPreference.update(port)
                dependencies
                    .syncModeOpener
                    .openSyncServer(port)
            }
        }
    }

    val openClient: StateFlow<(() -> Unit)?> = combineState(
        scope = scope,
        a = port,
        b = serverAddress,
    ) { portOrNull, serverAddressOrNull ->
        portOrNull?.let { port ->
            serverAddressOrNull?.let { serverAddress ->
                {
                    openSyncClient(
                        serverAddress = serverAddress,
                        port = port,
                    )
                }
            }
        }
    }

    private fun openSyncClient(
        serverAddress: ServerAddress,
        port: ServerPort,
    ) {
        scope.launch {
            inProgressRegistry.executeRegistered {
                serverAddressPreference.update(serverAddress)
                portPreference.update(port)
                dependencies
                    .syncModeOpener
                    .openSyncClient(serverAddress, port)
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}