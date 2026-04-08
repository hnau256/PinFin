package org.hnau.pinfin.model.sync

import kotlinx.serialization.Serializable
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

@Serializable
data class SyncConfig(
    val scheme: HttpScheme,
    val host: ServerHost,
)