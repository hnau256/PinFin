package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.state.AccountInfo

class TransactionTransferEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun accountChoose(): AccountChooseModel.Dependencies

        fun amountEnter(): AmountEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val from: AccountChooseModel.Skeleton,
        val to: AccountChooseModel.Skeleton,
        val amount: AmountEditModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                from = AccountChooseModel.Skeleton.createForNew(),
                to = AccountChooseModel.Skeleton.createForNew(),
                amount = AmountEditModel.Skeleton.createForNew(),
            )

            fun create(
                entry: Transaction.Type.Transfer<KeyValue<AccountId, AccountInfo>>,
            ): Skeleton = Skeleton(
                from = AccountChooseModel.Skeleton.create(
                    idWithAccount = entry.from,
                ),
                to = AccountChooseModel.Skeleton.create(
                    idWithAccount = entry.to,
                ),
                amount = AmountEditModel.Skeleton.create(
                    expression = entry.amount,
                )
            )
        }
    }

    val from = AccountChooseModel(
        scope = scope,
        skeleton = skeleton.from,
        dependencies = dependencies.accountChoose(),
        useMostPopularAccountAsDefault = false,
    )

    val to = AccountChooseModel(
        scope = scope,
        skeleton = skeleton.to,
        dependencies = dependencies.accountChoose(),
        useMostPopularAccountAsDefault = true,
    )

    val amount = AmountEditModel(
        scope = scope,
        skeleton = skeleton.amount,
        dependencies = dependencies.amountEnter(),
    )

    val transferEditable: StateFlow<Editable<Transaction.Type.Transfer<KeyValue<AccountId, AccountInfo>>>> =
        derivedStateFlowOf(scope) {
            editable {
                Transaction.Type.Transfer(
                    from = from.accountEditable.state.bind(),
                    to = to.accountEditable.state.bind(),
                    amount = amount.amountEditable.state.bind(),
                )
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}