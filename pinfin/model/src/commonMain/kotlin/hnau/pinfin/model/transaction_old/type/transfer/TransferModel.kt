@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction_old.type.transfer

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Transaction
import hnau.pinfin.model.AmountModel
import hnau.pinfin.model.transaction_old.type.utils.ChooseAccountModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransferModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        fun amount(): AmountModel.Dependencies

        fun chooseAccount(): ChooseAccountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val accounts: TransferSideValues<MutableStateFlow<AccountInfo?>>,
        val choosing: MutableStateFlow<Pair<TransferSide, ChooseAccountModel.Skeleton>?>,
        val amount: AmountModel.Skeleton,
    ) {

        constructor(
            type: TransactionInfo.Type.Transfer,
        ) : this(
            accounts = TransferSideValues(
                from = type.from.toMutableStateFlowAsInitial(),
                to = type.to.toMutableStateFlowAsInitial(),
            ),
            choosing = null.toMutableStateFlowAsInitial(),
            amount = AmountModel.Skeleton(
                amount = type.amount,
            )
        )

        companion object {

            val empty: Skeleton
                get()= Skeleton(
                accounts = TransferSideValues(
                    from = null.toMutableStateFlowAsInitial(),
                    to = null.toMutableStateFlowAsInitial(),
                ),
                choosing = null.toMutableStateFlowAsInitial(),
                amount = AmountModel.Skeleton.empty,
            )
        }
    }

    val amount = AmountModel(
        scope = scope,
        skeleton = skeleton.amount,
        dependencies = dependencies.amount(),
    )

    private val localUsedAccounts: StateFlow<Set<AccountInfo>> = combineState(
        scope = scope,
        a = skeleton.accounts.from,
        b = skeleton.accounts.to,
    ) { from, to ->
        setOfNotNull(from, to)
    }

    val choose: StateFlow<ChooseAccountModel?> = skeleton
        .choosing
        .mapWithScope(
            scope = scope,
        ) { stateScope, sideWithChooseAccountSkeletonOrNull ->
            sideWithChooseAccountSkeletonOrNull?.let { (side, chooseAccountSkeleton) ->
                ChooseAccountModel(
                    scope = stateScope,
                    skeleton = chooseAccountSkeleton,
                    dependencies = dependencies.chooseAccount(),
                    onReady = { skeleton.choosing.value = null },
                    selected = skeleton.accounts[side],
                    updateSelected = { skeleton.accounts[side].value = it },
                    localUsedAccounts = localUsedAccounts,
                )
            }
        }

    data class Account(
        val value: StateFlow<AccountInfo?>,
        val onClick: () -> Unit,
    )

    val accounts: TransferSideValues<Account> = skeleton
        .accounts
        .mapFull { side, selectedAccount ->
            Account(
                value = selectedAccount,
                onClick = {
                    skeleton
                        .choosing
                        .value = side to ChooseAccountModel.Skeleton.empty
                }
            )
        }

    val result: StateFlow<Transaction.Type.Transfer?> = combineState(
        scope = scope,
        a = skeleton.accounts.from,
        b = skeleton.accounts.to,
    ) { fromOrNull, toOrNull ->
        fromOrNull?.let { from ->
            toOrNull?.let { to ->
                from to to
            }
        }
    }.combineStateWith(
        scope = scope,
        other = amount.amount,
    ) { fromWithToOrNull, amountOrNull ->
        fromWithToOrNull?.let { (from, to) ->
            amountOrNull?.let { amount ->
                Transaction.Type.Transfer(
                    from = from.id,
                    to = to.id,
                    amount = amount,
                )
            }
        }
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}