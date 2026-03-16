package org.hnau.pinfin.model.sync

import org.hnau.pinfin.model.sync.utils.ServerAddress
import org.hnau.pinfin.model.sync.utils.ServerPort

data class SyncModeOpener(
    val openSyncServer: (port: ServerPort) -> Unit,
    val openSyncClient: (address: ServerAddress, port: ServerPort) -> Unit,
)