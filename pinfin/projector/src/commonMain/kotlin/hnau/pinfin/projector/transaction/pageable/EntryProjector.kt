package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction.pageable.EntryModel
import hnau.pinfin.model.utils.TemplateModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class EntryProjector(
    scope: CoroutineScope,
    model: EntryModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    class Page(
        scope: CoroutineScope,
        model: EntryModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies

        @Composable
        fun Content(
            contentPadding: PaddingValues,
            modifier: Modifier = Modifier,
        ) {
            //TODO()
        }
    }

    @Composable
    fun MainContent(
        modifier: Modifier = Modifier,
    ) {
        //TODO()
    }

    @Composable
    fun AmountContent(
        modifier: Modifier = Modifier,
    ) {
        //TODO()
    }
}