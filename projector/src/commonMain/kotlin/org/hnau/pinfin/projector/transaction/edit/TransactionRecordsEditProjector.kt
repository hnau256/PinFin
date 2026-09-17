package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.separator
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.coroutines.flow.state.mapZipListReusable
import org.hnau.pinfin.model.transaction.edit.TransactionRecordsEditModel
import org.hnau.pinfin.model.transaction.utils.RecordId

class TransactionRecordsEditProjector(
    scope: CoroutineScope,
    model: TransactionRecordsEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun record(): TransactionRecordEditProjector.Dependencies
    }

    val records: StateFlow<ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>> = model
        .records
        .mapZipListReusable(
            scope = scope,
            extractKey = { idWithRecord -> idWithRecord.key },
            transform = { _, idWithRecord ->
                KeyValue(
                    key = idWithRecord.key,
                    value = TransactionRecordEditProjector(
                        model = idWithRecord.value,
                        dependencies = dependencies.record(),
                    ),
                )
            },
        )

    fun Content(
        scope: SLazyTableScope,
        recordProjectors: ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>,
    ) {
        with(scope) {
            separator(key = "records_separator")
            val selected = recordProjectors.selected
            cells(
                items = recordProjectors,
                key = { it.key },
            ) { record ->
                record.value.Content(
                    selected = record === selected,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
