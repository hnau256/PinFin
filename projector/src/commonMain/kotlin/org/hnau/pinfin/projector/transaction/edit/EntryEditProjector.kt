package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.item
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.transaction.edit.TransactionEntryEditModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.resolvedDirection
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class EntryEditProjector(
    private val model: TransactionEntryEditModel,
    private val localization: Localization,
    private val amountFormatter: AmountFormatter,
    private val budgetRepository: BudgetRepository,
) {

    fun SLazyTableScope.Cells() {
        cell(key = "entry_account") {
            val projector = remember(model) {
                AccountChooseEditProjector(
                    model = model.account,
                    localization = localization,
                )
            }
            projector.Content(
                modifier = Modifier.animateItem(),
            )
        }
        item(key = "entry_total") {
            TotalContent(
                modifier = Modifier.animateItem(),
            )
        }
    }

    @Composable
    private fun TotalContent(
        modifier: Modifier = Modifier,
    ) {
        val currency = budgetRepository.state.collectAsState().value.info.currency
        val records = model.records.recordsEditable.collectAsState().value
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
                value = record.amount.toAmount(currency.scale),
            )
        }
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(localization.total)
            },
        ) {
            total.foldNullable(
                ifNull = {
                    SText(localization.amount)
                },
                ifNotNull = { totalNotNull ->
                    AmountContent(
                        value = totalNotNull,
                        amountFormatter = amountFormatter,
                    )
                },
            )
        }
    }
}
