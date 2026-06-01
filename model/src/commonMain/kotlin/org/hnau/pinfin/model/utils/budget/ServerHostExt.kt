package org.hnau.pinfin.model.utils.budget

import org.hnau.upchain.sync.core.ServerHost

val ServerHost.Companion.default: ServerHost
    get() = defaultServerHost

private val defaultServerHost: ServerHost =
    ServerHost.createOrNull("upchain.hnau.org")!!