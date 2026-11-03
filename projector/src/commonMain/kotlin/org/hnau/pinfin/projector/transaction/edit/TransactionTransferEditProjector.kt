package org.hnau.pinfin.projector.transaction.edit

import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.model.transaction.edit.TransactionTransferEditModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection

class TransactionTransferEditProjector(
    scope: CoroutineScope,
    model: TransactionTransferEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun accountChoose(): AccountChooseEditProjector.Dependencies

        fun amount(): AmountEditProjector.Dependencies
    }

    private val from = AccountChooseEditProjector(
        scope = scope,
        model = model.from,
        dependencies = dependencies.accountChoose(),
    )

    private val to = AccountChooseEditProjector(
        scope = scope,
        model = model.to,
        dependencies = dependencies.accountChoose(),
    )

    private val amount = AmountEditProjector(
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    val section: TransactionSection<Unit> = TransactionSection.composed(
        scope = scope,
        first = TransactionSection.composed(
            scope = scope,
            first = from.section,
            second = to.section,
            joiner = Mapper(
                direct = {},
                reverse = { Unit to Unit },
            ),
        ),
        second = amount.section,
        joiner = Mapper(
            direct = {},
            reverse = { Unit to Unit },
        ),
    )
}
