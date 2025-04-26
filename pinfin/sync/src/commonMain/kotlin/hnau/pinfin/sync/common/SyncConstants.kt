package hnau.pinfin.sync.common

import hnau.pinfin.sync.server.dto.ServerPort
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

object SyncConstants {

    val defaultPort: ServerPort = ServerPort(27436)

    @OptIn(ExperimentalSerializationApi::class)
    val cbor: Cbor = Cbor
}