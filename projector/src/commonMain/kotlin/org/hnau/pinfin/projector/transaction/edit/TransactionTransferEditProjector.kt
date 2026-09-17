package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.TransactionTransferEditModel

class TransactionTransferEditProjector(
    model: TransactionTransferEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun from(): AccountChooseEditProjector.Dependencies

        fun to(): AccountChooseEditProjector.Dependencies

        fun amount(): AmountEditProjector.Dependencies
    }

    private val from = AccountChooseEditProjector(
        model = model.from,
        dependencies = dependencies.from(),
    )

    private val to = AccountChooseEditProjector(
        model = model.to,
        dependencies = dependencies.to(),
    )

    private val amount = AmountEditProjector(
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    fun Content(
        scope: SLazyTableScope,
    ) {
        with(scope) {
            cell(key = "transfer_from") {
                from.Content(
                    modifier = Modifier.animateItem(),
                )
            }
            cell(key = "transfer_to") {
                to.Content(
                    modifier = Modifier.animateItem(),
                )
            }
            cell(key = "transfer_amount") {
                amount.Content(
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
