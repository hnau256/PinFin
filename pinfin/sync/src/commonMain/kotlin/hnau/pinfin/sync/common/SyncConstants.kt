package hnau.pinfin.sync.common

import hnau.pinfin.sync.server.ServerPort
import java.nio.charset.Charset

object SyncConstants {

    val charset: Charset = Charsets.UTF_8

    val defaultPort: ServerPort = ServerPort(27436)
}