package org.hnau.pinfin.projector.transaction.edit

import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.model.transaction.edit.TransactionEntryEditModel
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class TransactionEntryEditProjector(
    scope: CoroutineScope,
    private val model: TransactionEntryEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val amountFormatter: AmountFormatter

        fun account(): AccountChooseEditProjector.Dependencies

        fun records(): TransactionRecordsEditProjector.Dependencies
    }

    val section: TransactionSection<ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>> = TransactionSection.composed(
        scope = scope,
        first = AccountChooseEditProjector(
            model = model.account,
            dependencies = dependencies.account(),
        ).section,
        second = TransactionRecordsEditProjector(
            scope = scope,
            model = model.records,
            dependencies = dependencies.records(),
        ).section,
        joiner = Mapper(
            direct = { (_, records) -> records },
            reverse = { Unit to it }
        )
    )
}
