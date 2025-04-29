package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.utils.TemplateModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncServerProjector(
    scope: CoroutineScope,
    model: SyncServerModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}