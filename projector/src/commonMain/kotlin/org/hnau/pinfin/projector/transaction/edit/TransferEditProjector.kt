package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.pinfin.model.transaction.edit.TransactionTransferEditModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class TransferEditProjector(
    private val model: TransactionTransferEditModel,
    private val localization: Localization,
    private val amountFormatter: AmountFormatter,
    private val budgetRepository: BudgetRepository,
) {

    fun SLazyTableScope.Cells() {
        cell(key = "transfer_from") {
            val projector = remember(model) {
                AccountChooseEditProjector(
                    model = model.from,
                    localization = localization,
                )
            }
            projector.Content(
                modifier = Modifier.animateItem(),
            )
        }
        cell(key = "transfer_to") {
            val projector = remember(model) {
                AccountChooseEditProjector(
                    model = model.to,
                    localization = localization,
                )
            }
            projector.Content(
                modifier = Modifier.animateItem(),
            )
        }
        cell(key = "transfer_amount") {
            val projector = remember(model) {
                AmountEditProjector(
                    model = model.amount,
                    localization = localization,
                    amountFormatter = amountFormatter,
                    budgetRepository = budgetRepository,
                )
            }
            projector.Content(
                modifier = Modifier.animateItem(),
            )
        }
    }
}
