package org.hnau.pinfin.model.transaction.edit

import arrow.core.identity
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountInfo

class AccountChooseModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
    useMostPopularAccountAsDefault: Boolean,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val initialIdWithAccount: KeyValue<AccountId, AccountInfo>?,
        val choose: ChooseOrCreateModel.Skeleton = ChooseOrCreateModel.Skeleton(),
        val manualIdWithAccount: MutableStateFlow<KeyValue<AccountId, AccountInfo>?> = initialIdWithAccount.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialIdWithAccount = null,
            )

            fun create(
                idWithAccount: KeyValue<AccountId, AccountInfo>,
            ): Skeleton = Skeleton(
                initialIdWithAccount = idWithAccount,
            )
        }
    }

    private val selectedAccount: StateFlow<KeyValue<AccountId, AccountInfo>?> = skeleton
        .manualIdWithAccount
        .flatMapWithScope(scope) { scope, manualAccountOrNull ->
            manualAccountOrNull
                .foldNullable(
                    ifNotNull = { manual ->
                        manual.toMutableStateFlowAsInitial()
                    },
                    ifNull = {
                        useMostPopularAccountAsDefault.foldBoolean(
                            ifFalse = { null.toMutableStateFlowAsInitial() },
                            ifTrue = { resolveMostPopularAccount(scope) },
                        )
                    }
                )
        }

    val choose: ChooseOrCreateModel<KeyValue<AccountId, AccountInfo>> = ChooseOrCreateModel(
        scope = scope,
        skeleton = skeleton.choose,
        variants = dependencies
            .budgetRepository
            .state
            .mapState(scope) { state ->
                state.accounts
            },
        selected = selectedAccount.mapState(scope) { it.toOption() },
        onSelectedChanged = skeleton.manualIdWithAccount::value::set,
        createAdditionalVariants = { query ->
            val id = AccountId(query)
            listOf(
                KeyValue(
                    key = id,
                    value = AccountInfo.create(
                        id = id,
                        config = null,
                        amount = KeyValue(AmountDirection.Credit, Amount.zero),
                    )
                )
            )
        },
        extractKey = { it.key.id },
        extractTitle = { it.value.title },
    )

    val accountEditable: StateFlow<Editable<KeyValue<AccountId, AccountInfo>>> =
        Editable.create(
            scope = scope,
            valueOrNone = selectedAccount.mapState(scope) { it.toOption() },
            initialValueOrNone = skeleton.initialIdWithAccount.toOption(),
        )

    private fun resolveMostPopularAccount(
        scope: CoroutineScope,
    ): StateFlow<KeyValue<AccountId, AccountInfo>?> = dependencies
        .budgetRepository
        .state
        .map { state ->
            state
                .transactions
                .sortedByDescending { idWithTransaction ->
                    idWithTransaction.value.timestamp
                }
                .take(16)
                .map { idWithTransaction ->
                    idWithTransaction.value.type.fold(
                        ifEntry = { idWithAccount, _ -> idWithAccount },
                        ifTransfer = { from, _, _ -> from },
                    )
                }
                .groupBy(::identity)
                .maxByOrNull { it.value.size }
                ?.key
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val goBackHandler: GoBackHandler
        get() = choose.goBackHandler
}