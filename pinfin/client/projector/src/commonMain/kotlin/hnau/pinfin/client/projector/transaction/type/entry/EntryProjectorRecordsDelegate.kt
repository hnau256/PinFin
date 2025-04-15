package hnau.pinfin.client.projector.transaction.type.entry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.util.fastForEach
import arrow.core.NonEmptyList
import hnau.common.compose.uikit.ContainerStyle
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.utils.Icon
import hnau.common.kotlin.coroutines.mapNonEmptyListReusable
import hnau.pinfin.client.model.transaction.type.entry.EntryModel
import hnau.pinfin.client.model.transaction.type.entry.record.RecordId
import hnau.pinfin.client.projector.transaction.type.entry.record.RecordProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.add_record

class EntryProjectorRecordsDelegate(
    scope: CoroutineScope,
    private val model: EntryModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun record(): RecordProjector.Dependencies

    }

    private data class Item(
        val id: RecordId,
        val projector: RecordProjector,
    )

    private val records: StateFlow<NonEmptyList<Item>> = model
        .records
        .mapNonEmptyListReusable(
            scope = scope,
            extractKey = { it.id },
            transform = { scope, record ->
                Item(
                    id = record.id,
                    projector = RecordProjector(
                        scope = scope,
                        model = record.model,
                        dependencies = dependencies.record(),
                    )
                )
            },
        )

    @Composable
    fun Content() {
        records
            .collectAsState()
            .value
            .fastForEach { item ->
                key(item.id.id) {
                    item.projector.Content()
                }
            }
        HnauButton(
            content = {
                TripleRow(
                    content = { Text(stringResource(Res.string.add_record)) },
                    leading = { Icon { Icons.Filled.Add } }
                )
            },
            onClick = model::addNewRecord,
            style = ContainerStyle.Tertiary,
        )
    }

}