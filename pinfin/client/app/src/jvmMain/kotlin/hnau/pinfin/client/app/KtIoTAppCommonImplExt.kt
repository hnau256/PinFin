package hnau.pinfin.client.app

import hnau.common.app.storage.Storage

actual fun PinFinApp.Dependencies.Companion.commonImpl(
    storageFactory: Storage.Factory,
): PinFinApp.Dependencies = PinFinApp.Dependencies.impl()