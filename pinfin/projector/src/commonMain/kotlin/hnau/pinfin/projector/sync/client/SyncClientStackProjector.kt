package hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncClientStackProjector(
    scope: CoroutineScope,
    model: SyncClientStackModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}