package hnau.pinfin.client.app

import hnau.common.app.storage.Storage

expect fun PinFinApp.Dependencies.Companion.commonImpl(
    storageFactory: Storage.Factory,
): PinFinApp.Dependencies