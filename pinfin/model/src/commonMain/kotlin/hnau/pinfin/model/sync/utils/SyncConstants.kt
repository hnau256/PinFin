package hnau.pinfin.model.sync.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

object SyncConstants {

    @OptIn(ExperimentalSerializationApi::class)
    val cbor: Cbor = Cbor
}