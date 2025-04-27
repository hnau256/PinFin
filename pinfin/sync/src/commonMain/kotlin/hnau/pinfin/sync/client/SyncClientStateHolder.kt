package hnau.pinfin.sync.client

interface SyncClientStateHolder<S: SyncClientState> {

    val state: S
}