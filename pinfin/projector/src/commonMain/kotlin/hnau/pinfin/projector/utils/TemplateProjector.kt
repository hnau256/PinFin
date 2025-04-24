package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.utils.choose.TemplateModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class TemplateProjector(
    scope: CoroutineScope,
    model: TemplateModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}