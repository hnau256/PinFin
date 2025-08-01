package hnau.pinfin.projector.transaction_old.type.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.pinfin.model.transaction_old.type.entry.EntryModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class EntryProjector(
    private val scope: CoroutineScope,
    private val model: EntryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun header(): EntryProjectorHeaderDelegate.Dependencies

        fun records(): EntryProjectorRecordsDelegate.Dependencies
    }

    private val headerDelegate = EntryProjectorHeaderDelegate(
        scope = scope,
        dependencies = dependencies.header(),
        model = model,
    )

    private val recordsDelegate = EntryProjectorRecordsDelegate(
        scope = scope,
        dependencies = dependencies.records(),
        model = model,
    )

    @Composable
    fun Content() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            headerDelegate.Content()
            recordsDelegate.Content()
        }
    }
}