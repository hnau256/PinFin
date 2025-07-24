@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
)

package hnau.pinfin.model.transaction.type.entry

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapNonEmptyListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.data.Transaction
import hnau.pinfin.model.transaction.type.entry.record.RecordId
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.model.transaction.type.utils.ChooseAccountModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class EntryModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val manualAccount: MutableStateFlow<AccountInfo?>,
        val records: MutableStateFlow<NonEmptyList<Pair<RecordId, RecordModel.Skeleton>>>,
        val chooseAccount: MutableStateFlow<ChooseAccountModel.Skeleton?> = null.toMutableStateFlowAsInitial(),
    ) {

        constructor(
            type: TransactionInfo.Type.Entry,
        ) : this(
            manualAccount = type.account.toMutableStateFlowAsInitial(),
            records = type
                .records
                .map { record ->
                    val id = RecordId.Companion.new()
                    val skeleton = RecordModel.Skeleton(
                        record = record,
                    )
                    id to skeleton
                }
                .toMutableStateFlowAsInitial()
        )

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    manualAccount = null.toMutableStateFlowAsInitial(),
                    records = nonEmptyListOf(
                        RecordId.Companion.new() to RecordModel.Skeleton.empty,
                    ).toMutableStateFlowAsInitial(),
                )
        }
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun record(): RecordModel.Dependencies

        fun chooseAccount(): ChooseAccountModel.Dependencies
    }

    data class RecordItem(
        val id: RecordId,
        val model: RecordModel,
        val scope: CoroutineScope,
    )

    val records: StateFlow<NonEmptyList<RecordItem>> = run {

        val localUsedCategories = skeleton
            .records
            .scopedInState(
                parentScope = scope,
            )
            .flatMapState(
                scope = scope,
            ) { (recordsScope, records) ->
                records
                    .fold<_, StateFlow<Set<CategoryInfo>>>(
                        initial = emptySet<CategoryInfo>().toMutableStateFlowAsInitial()
                    ) { accState, record ->
                        accState
                            .combineStateWith(
                                scope = recordsScope,
                                other = record.second.manualCategory,
                            ) { acc, categoryOrNull ->
                                categoryOrNull
                                    ?.let { category -> acc + category }
                                    ?: acc
                            }
                    }
            }

        skeleton
            .records
            .mapNonEmptyListReusable(
                scope = scope,
                extractKey = Pair<RecordId, *>::first,
                transform = { itemScope, (id, skeleton) ->
                    RecordItem(
                        id = id,
                        model = RecordModel(
                            scope = itemScope,
                            dependencies = dependencies.record(),
                            skeleton = skeleton,
                            remove = this@EntryModel
                                .skeleton
                                .records
                                .mapState(
                                    scope = itemScope,
                                ) { allRecords ->
                                    val newRecordsOrNull = allRecords
                                        .filter { it.first != id }
                                        .toNonEmptyListOrNull()
                                    newRecordsOrNull?.let { newRecords ->
                                        {
                                            this@EntryModel
                                                .skeleton
                                                .records
                                                .value = newRecords
                                        }
                                    }
                                },
                            localUsedCategories = localUsedCategories,
                            createNextIfLast = this@EntryModel
                                .skeleton
                                .records
                                .mapState(
                                    scope = itemScope,
                                ) { records ->
                                    val isLast = records.last().first == id
                                    if (!isLast) {
                                        return@mapState null
                                    }
                                    ::addNewRecord
                                }
                        ),
                        scope = scope,
                    )
                },
            )
    }

    val amount: StateFlow<Amount> = records
        .scopedInState(
            parentScope = scope,
        )
        .flatMapState(
            scope = scope,
        ) { (recordsScope, records) ->
            records
                .fold<_, StateFlow<Amount>>(
                    initial = Amount.zero.toMutableStateFlowAsInitial(),
                ) { accState, (_, recordModel) ->
                    accState
                        .combineStateWith(
                            scope = recordsScope,
                            other = recordModel.record,
                        ) { acc, recordOrNull ->
                            acc + (recordOrNull?.amount ?: Amount.zero)
                        }
                }
        }

    private val lastTransactionAccount: StateFlow<AccountInfo?> = dependencies
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
                        is TransactionInfo.Type.Transfer -> type.to
                    }
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


    val account: StateFlow<AccountInfo?> = skeleton
        .manualAccount
        .flatMapState(scope) { manualAccountOrNull ->
            manualAccountOrNull.foldNullable<AccountInfo, StateFlow<AccountInfo?>>(
                ifNotNull = AccountInfo::toMutableStateFlowAsInitial,
                ifNull = { lastTransactionAccount }
            )
        }

    private val localUsedAccounts: StateFlow<Set<AccountInfo>> = account.mapState(
        scope = scope,
    ) { accountOrNull ->
        setOfNotNull(accountOrNull)
    }

    val chooseAccount: StateFlow<ChooseAccountModel?> = skeleton
        .chooseAccount
        .mapWithScope(
            scope = scope,
        ) { stateScope, chooseAccountSkeletonOrNull ->
            chooseAccountSkeletonOrNull?.let { chooseAccountSkeleton ->
                ChooseAccountModel(
                    scope = stateScope,
                    skeleton = chooseAccountSkeleton,
                    dependencies = dependencies.chooseAccount(),
                    localUsedAccounts = localUsedAccounts,
                    selected = account,
                    updateSelected = { skeleton.manualAccount.value = it },
                    onReady = { skeleton.chooseAccount.value = null },
                )
            }
        }

    fun chooseAccount() {
        skeleton.chooseAccount.value = ChooseAccountModel.Skeleton.empty
    }

    fun addNewRecord() {
        skeleton.records.update { records ->
            val newItem = RecordId.Companion.new() to RecordModel.Skeleton.empty
            records + newItem
        }
    }

    val result: StateFlow<Transaction.Type.Entry?> = records
        .scopedInState(
            parentScope = scope,
        )
        .flatMapState(
            scope = scope,
        ) { (recordsScope, records) ->
            records
                .tail
                .fold(
                    initial = records.head.model.record.mapState(recordsScope) { recordOrNull ->
                        recordOrNull?.let {
                            nonEmptyListOf(it)
                        }
                    }
                ) { accState, item ->
                    accState
                        .combineStateWith(
                            scope = recordsScope,
                            other = item.model.record,
                        ) { accOrNull, recordOrNull ->
                            accOrNull?.let { acc ->
                                recordOrNull?.let { record ->
                                    acc + record
                                }
                            }
                        }
                }
        }
        .combineStateWith(
            scope = scope,
            other = account,
        ) { recordsOrNull, accountOrNull ->
            recordsOrNull?.let { records ->
                accountOrNull?.let { account ->
                    Transaction.Type.Entry(
                        records = records,
                        account = account.id,
                    )
                }
            }
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}