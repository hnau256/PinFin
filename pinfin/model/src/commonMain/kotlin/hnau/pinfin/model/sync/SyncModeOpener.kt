package hnau.pinfin.model.sync

import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort

data class SyncModeOpener(
    val openSyncServer: (port: ServerPort) -> Unit,
    val openSyncClient: (address: ServerAddress, port: ServerPort) -> Unit,
)