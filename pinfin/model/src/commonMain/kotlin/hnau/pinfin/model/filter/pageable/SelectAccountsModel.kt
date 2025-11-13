@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.filter.pageable

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapMutableState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.AccountId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SelectAccountsModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val selectedAccounts: MutableStateFlow<Set<AccountId>>,
    ) {

        companion object {

            fun create(
                initialSelectedAccountsIds: NonEmptySet<AccountId>?,
            ): Skeleton = Skeleton(
                selectedAccounts = initialSelectedAccountsIds
                    .ifNull { emptyList() }
                    .toSet()
                    .toMutableStateFlowAsInitial(),
            )
        }
    }

    class Page(
        val accounts: StateFlow<List<Account>>,
    ) {

        data class Account(
            val info: AccountInfo,
            val selected: MutableStateFlow<Boolean>,
        )

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    private val accounts: StateFlow<List<Page.Account>> = dependencies
        .budgetRepository
        .state
        .mapWithScope(scope) { scope, state ->
            state
                .accounts
                .map { info ->
                    val id = info.id

                    val updateIds: (Set<AccountId>, Boolean) -> Set<AccountId> =
                        { selectedIds, selected ->
                            selected.foldBoolean(
                                ifTrue = { selectedIds + id },
                                ifFalse = { selectedIds - id }
                            )
                        }

                    Page.Account(
                        info = info,
                        selected = skeleton
                            .selectedAccounts
                            .mapMutableState(
                                scope = scope,
                                transform = { selectedIds -> id in selectedIds },
                                set = { selected ->
                                    update { selectedIds ->
                                        updateIds(selectedIds, selected)
                                    }
                                },
                                compareAndSet = { expectSelected, selected ->
                                    val current = value
                                    compareAndSet(
                                        updateIds(current, expectSelected),
                                        updateIds(current, selected),
                                    )
                                },
                            )
                    )
                }
        }

    val selectedAccounts: StateFlow<NonEmptyList<AccountInfo>?> = accounts
        .mapWithScope(scope) { scope, accounts ->
            accounts.map { account ->
                account
                    .selected
                    .mapState(scope) { selected ->
                        selected.ifTrue { account.info }
                    }
            }
        }
        .flatMapWithScope(
            scope = scope,
        ) { scope, accounts ->
            accounts
                .drop(1)
                .fold(
                    initial = accounts
                        .firstOrNull()
                        .foldNullable(
                            ifNull = { emptySet<AccountInfo>().toMutableStateFlowAsInitial() },
                            ifNotNull = { first ->
                                first.mapState(scope) { setOfNotNull(it) }
                            }
                        ),
                ) { acc, accountOrNull ->
                    acc
                        .combineStateWith(
                            scope = scope,
                            other = accountOrNull,
                        ) { acc, accountOrNull ->
                            accountOrNull.foldNullable(
                                ifNull = { acc },
                                ifNotNull = { acc + it }
                            )
                        }
                }
                .mapState(scope) { accounts ->
                    accounts
                        .toList()
                        .sorted()
                        .toNonEmptyListOrNull()
                }
        }

    val selectedAccountsIds: StateFlow<NonEmptySet<AccountId>?> = selectedAccounts
        .mapState(scope) { accounts ->
            accounts
                ?.map { account -> account.id }
                ?.toNonEmptySet()
        }

    fun clear() {
        skeleton.selectedAccounts.value = emptySet()
    }

    fun createPage(): Page = Page(
        accounts = accounts,
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}