@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.records.RecordEntry
import org.hnau.pinfin.data.records.SimpleRecord
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionEntryEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun account(): AccountChooseModel.Dependencies

        fun records(): TransactionRecordsEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val account: AccountChooseModel.Skeleton,
        val records: TransactionRecordsEditModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                account = AccountChooseModel.Skeleton.createForNew(),
                records = TransactionRecordsEditModel.Skeleton.createForNew(),
            )

            fun create(
                entry: Transaction.Type.Entry<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
            ): Skeleton = Skeleton(
                account = AccountChooseModel.Skeleton.create(
                    idWithAccount = entry.account,
                ),
                records = TransactionRecordsEditModel.Skeleton.create(
                    records = entry.records.map(RecordEntry<KeyValue<CategoryId, CategoryInfo>>::record),
                ),
            )
        }
    }

    val account = AccountChooseModel(
        scope = scope,
        skeleton = skeleton.account,
        dependencies = dependencies.account(),
        useMostPopularAccountAsDefault = true,
    )

    val records = TransactionRecordsEditModel(
        scope = scope,
        skeleton = skeleton.records,
        dependencies = dependencies.records(),
    )

    val entryEditable: StateFlow<Editable<Transaction.Type.Entry<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, RecordEntry<KeyValue<CategoryId, CategoryInfo>>>>> = derivedStateFlowOf(scope) {
        editable {
            Transaction.Type.Entry(
                account = account.accountEditable.state.bind(),
                records = records.recordsEditable.state.bind().map(::SimpleRecord),
            )
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}