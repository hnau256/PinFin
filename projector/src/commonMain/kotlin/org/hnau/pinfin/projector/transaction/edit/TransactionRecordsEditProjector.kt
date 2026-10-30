package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.separator
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.coroutines.flow.state.mapZipListReusable
import org.hnau.pinfin.model.transaction.edit.TransactionRecordsEditModel
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class TransactionRecordsEditProjector(
    scope: CoroutineScope,
    private val model: TransactionRecordsEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization

        fun record(): TransactionRecordEditProjector.Dependencies
    }

    val section: TransactionSection<ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>> =
        TransactionSection(
            state = model
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
                ),
            content = { records: ZipList<KeyValue<RecordId, TransactionRecordEditProjector>> ->
                cell(
                    key = "records_amount",
                ) {
                    SPanel {
                        SItem(
                            topAccessory = {
                                SText(dependencies.localization.total)
                            },
                        ) {
                            AmountContent(
                                value = model.totalAmount.collectAsState().value,
                                amountFormatter = dependencies.amountFormatter,
                            )
                        }
                    }
                }
                separator(key = "records_separator")
                cells(
                    items = records,
                    key = { it.key },
                ) { record ->
                    record.value.Content(
                        selected = record === records.selected,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        )
}
