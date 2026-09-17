package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.item
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.transaction.edit.TransactionEntryEditModel
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.model.utils.resolvedDirection
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class TransactionEntryEditProjector(
    scope: CoroutineScope,
    private val model: TransactionEntryEditModel,
    private val currency: StateFlow<Currency>,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val amountFormatter: AmountFormatter

        fun account(): AccountChooseEditProjector.Dependencies

        fun records(): TransactionRecordsEditProjector.Dependencies
    }

    private val account = AccountChooseEditProjector(
        model = model.account,
        dependencies = dependencies.account(),
    )

    val records = TransactionRecordsEditProjector(
        scope = scope,
        model = model.records,
        dependencies = dependencies.records(),
    )

    fun Content(
        scope: SLazyTableScope,
        recordProjectors: ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>,
    ) {
        with(scope) {
            cell(key = "entry_account") {
                account.Content(
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "entry_total") {
                TotalContent(
                    modifier = Modifier.animateItem(),
                )
            }
        }
        records.Content(
            scope = scope,
            recordProjectors = recordProjectors,
        )
    }

    @Composable
    private fun TotalContent(
        modifier: Modifier = Modifier,
    ) {
        val currencyValue by currency.collectAsState()
        val records by model.records.recordsEditable.collectAsState()
        val recordsValue = records.fold(
            ifIncorrect = { null },
            ifValue = { value, _ -> value },
        )
        val total = recordsValue?.fold(
            initial = KeyValue(
                key = AmountDirection.Credit,
                value = Amount.zero,
            ),
        ) { acc, record ->
            acc + KeyValue(
                key = record.resolvedDirection,
                value = record.amount.toAmount(currencyValue.scale),
            )
        }
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(dependencies.localization.total)
            },
        ) {
            total.foldNullable(
                ifNull = {
                    SText(dependencies.localization.amount)
                },
                ifNotNull = { totalNotNull ->
                    AmountContent(
                        value = totalNotNull,
                        amountFormatter = dependencies.amountFormatter,
                    )
                },
            )
        }
    }
}
