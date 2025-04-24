package hnau.pinfin.projector

import androidx.compose.runtime.Composable
import hnau.pinfin.model.SyncModel
import hnau.pinfin.model.utils.choose.TemplateModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncProjector(
    scope: CoroutineScope,
    model: SyncModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}