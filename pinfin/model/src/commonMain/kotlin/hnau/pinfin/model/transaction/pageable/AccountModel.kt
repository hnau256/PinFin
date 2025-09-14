@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.identity
import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.valueOrNone
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AccountModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
    private val useMostPopularAccountAsDefault: Boolean,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun chooseOrCreate(): ChooseOrCreateModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val initialAccount: AccountInfo?,
        var chooseOrCreate: ChooseOrCreateModel.Skeleton? = null,
        val manualAccount: MutableStateFlow<AccountInfo?> = initialAccount.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialAccount = null,
            )

            fun createForEdit(
                account: AccountInfo,
            ): Skeleton = Skeleton(
                initialAccount = account,
            )
        }
    }


    private fun resolveMostPopularAccount(
        scope: CoroutineScope,
    ): StateFlow<Option<AccountInfo>> = dependencies
        .budgetRepository
        .state
        .map { state ->
            state
                .transactions
                .sortedByDescending { it.timestamp }
                .take(16)
                .map { transaction ->
                    when (val type = transaction.type) {
                        is TransactionInfo.Type.Entry -> type.account
                        is TransactionInfo.Type.Transfer -> type.from
                    }
                }
                .groupBy(::identity)
                .maxByOrNull { it.value.size }
                ?.key
                .toOption()
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = None,
        )

    internal val accountEditable: StateFlow<Editable<AccountInfo>> = Editable.create(
        scope = scope,
        valueOrNone = skeleton
            .manualAccount
            .flatMapWithScope(scope) { scope, manualAccountOrNull ->
                manualAccountOrNull
                    .toOption()
                    .fold(
                        ifSome = { Some(it).toMutableStateFlowAsInitial() },
                        ifEmpty = {
                            useMostPopularAccountAsDefault.foldBoolean(
                                ifFalse = { None.toMutableStateFlowAsInitial() },
                                ifTrue = { resolveMostPopularAccount(scope) },
                            )
                        }
                    )
            },
        initialValueOrNone = skeleton.initialAccount.toOption(),
    )

    val account: StateFlow<AccountInfo?> = accountEditable
        .mapState(scope) { it.valueOrNone.getOrNull() }

    fun createPage(
        scope: CoroutineScope,
    ): ChooseOrCreateModel<AccountInfo> = ChooseOrCreateModel(
        scope = scope,
        dependencies = dependencies.chooseOrCreate(),
        skeleton = skeleton::chooseOrCreate
            .toAccessor()
            .getOrInit { ChooseOrCreateModel.Skeleton() },
        extractItemsFromState = BudgetState::visibleAccounts,
        additionalItems = accountEditable.mapState(scope) { listOfNotNull(it.valueOrNone.getOrNull()) },
        itemTextMapper = Mapper(
            direct = AccountInfo::title,
            reverse = { title ->
                AccountInfo(
                    id = AccountId(title),
                    config = null,
                    amount = Amount.zero,
                )
            }
        ),
        selected = accountEditable.mapState(scope, Editable<AccountInfo>::valueOrNone),
        onReady = { selected ->
            skeleton.manualAccount.value = selected
            goForward()
        }
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}