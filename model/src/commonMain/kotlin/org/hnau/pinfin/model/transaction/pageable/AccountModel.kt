@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.identity
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

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
        val initialIdWithAccount: KeyValue<AccountId, AccountInfo>?,
        var chooseOrCreate: ChooseOrCreateModel.Skeleton? = null,
        val manualIdWithAccount: MutableStateFlow<KeyValue<AccountId, AccountInfo>?> = initialIdWithAccount.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialIdWithAccount = null,
            )

            fun createForEdit(
                idWithAccount: KeyValue<AccountId, AccountInfo>,
            ): Skeleton = Skeleton(
                initialIdWithAccount = idWithAccount,
            )
        }
    }


    private fun resolveMostPopularAccount(
        scope: CoroutineScope,
    ): StateFlow<Option<KeyValue<AccountId, AccountInfo>>> = dependencies
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
                    when (val type = idWithTransaction.value.type) {
                        is TransactionInfo.Type.Entry -> type.idWithAccount
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

    internal val accountEditable: StateFlow<Editable<KeyValue<AccountId, AccountInfo>>> =
        Editable.create(
            scope = scope,
            valueOrNone = skeleton
                .manualIdWithAccount
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
            initialValueOrNone = skeleton.initialIdWithAccount.toOption(),
        )

    val idWithAccount: StateFlow<KeyValue<AccountId, AccountInfo>?> = accountEditable
        .mapState(scope) { it.valueOrNone.getOrNull() }

    fun createPage(
        scope: CoroutineScope,
    ): ChooseOrCreateModel<KeyValue<AccountId, AccountInfo>> = ChooseOrCreateModel(
        scope = scope,
        dependencies = dependencies.chooseOrCreate(),
        skeleton = skeleton::chooseOrCreate
            .toAccessor()
            .getOrInit { ChooseOrCreateModel.Skeleton() },
        extractItemsFromState = BudgetState::visibleAccounts,
        additionalItems = accountEditable.mapState(scope) { listOfNotNull(it.valueOrNone.getOrNull()) },
        itemTextMapper = Mapper(
            direct = { it.value.title },
            reverse = { title ->
                val id = AccountId(title)
                val accountInfo = AccountInfo(
                    id = id,
                    config = null,
                    amount = Amount.zero,
                )
                KeyValue(id, accountInfo)
            }
        ),
        selected = accountEditable.mapState(
            scope,
            Editable<KeyValue<AccountId, AccountInfo>>::valueOrNone
        ),
        onReady = { selected ->
            skeleton.manualIdWithAccount.value = selected
            goForward()
        },
        comparator = compareBy(KeyValue<*, AccountInfo>::value),
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}