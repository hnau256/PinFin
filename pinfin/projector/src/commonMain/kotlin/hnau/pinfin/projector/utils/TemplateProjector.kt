package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.utils.TemplateModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TemplateProjector(
    scope: CoroutineScope,
    model: TemplateModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

    }

    @Composable
    fun Content() {

    }
}