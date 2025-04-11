package hnau.pinfin.client.projector.init

import androidx.compose.runtime.Composable
import hnau.pinfin.client.model.init.InitModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class InitProjector(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    model: InitModel,
) {

    @Shuffle
    interface Dependencies {

        companion object
    }

    @Composable
    fun Content() {
    }
}