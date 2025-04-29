package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable
import hnau.pinfin.model.sync.client.SyncClientModel
import hnau.pinfin.model.utils.TemplateModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncClientProjector(
    scope: CoroutineScope,
    model: SyncClientModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}