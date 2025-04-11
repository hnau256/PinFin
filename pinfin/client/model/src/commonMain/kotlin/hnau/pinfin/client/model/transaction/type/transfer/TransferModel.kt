@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.transaction.type.transfer

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.model.AmountModel
import hnau.pinfin.client.model.transaction.type.utils.ChooseAccountModel
import hnau.pinfin.scheme.AccountId
import hnau.pinfin.scheme.Transaction
import hnau.shuffler.annotations.Shuffle
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

    @Shuffle
    interface Dependencies {

        fun amount(): AmountModel.Dependencies

        fun chooseAccount(): ChooseAccountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val accounts: TransferSideValues<MutableStateFlow<AccountId?>>,
        val choosing: MutableStateFlow<Pair<TransferSide, ChooseAccountModel.Skeleton>?>,
        val amount: AmountModel.Skeleton,
    ) {

        constructor(
            type: Transaction.Type.Transfer,
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

    private val localUsedAccounts: StateFlow<Set<AccountId>> = combineState(
        scope = scope,
        a = skeleton.accounts.from,
        b = skeleton.accounts.to,
    ) { from, to ->
        setOfNotNull(
            from, to
        )
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
        val value: StateFlow<AccountId?>,
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
                    from = from,
                    to = to,
                    amount = amount,
                )
            }
        }
    }
}