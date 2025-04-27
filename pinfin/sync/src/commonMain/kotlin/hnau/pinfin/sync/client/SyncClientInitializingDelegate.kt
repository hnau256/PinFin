package hnau.pinfin.sync.client

import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.upchain.BudgetsStorage
import kotlinx.coroutines.CoroutineScope

class SyncClientInitializingDelegate(
    scope: CoroutineScope,
    syncApi: SyncApi,
    budgetsStorage: BudgetsStorage,
    onInitialized: (SyncClientStateHolder<SyncClientState.Budgets>) -> Unit,
): SyncClientStateHolder<SyncClientState.Initializing> {

    override val state: SyncClientState.Initializing
        get() = SyncClientState.Initializing
}