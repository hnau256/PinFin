@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction

import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.toEditingString
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.budget.BudgetRepository
import hnau.pinfin.data.budget.TransactionInfo
import hnau.pinfin.model.transaction.type.TransactionTypeModel
import hnau.pinfin.model.transaction.type.entry.EntryModel
import hnau.pinfin.model.transaction.type.transfer.TransferModel
import hnau.pinfin.data.dto.Comment
import hnau.pinfin.data.dto.Transaction
import hnau.pinfin.data.dto.TransactionType
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransactionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val completed: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val id: Transaction.Id?,
        val comment: MutableStateFlow<EditingString>,
        val date: MutableStateFlow<LocalDate>,
        val time: MutableStateFlow<LocalTime>,
        val type: MutableStateFlow<TransactionTypeModel.Skeleton>,
    ) {

        companion object {

            fun createForEdit(
                info: TransactionInfo,
            ): Skeleton {
                val localDateTime =
                    info.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                return Skeleton(
                    id = info.id,
                    comment = info.comment.text
                        .toEditingString()
                        .toMutableStateFlowAsInitial(),
                    date = localDateTime.date.toMutableStateFlowAsInitial(),
                    time = localDateTime.time.toMutableStateFlowAsInitial(),
                    type = info
                        .type
                        .let { type ->
                            when (type) {
                                is TransactionInfo.Type.Entry -> TransactionTypeModel.Skeleton.Entry(
                                    skeleton = EntryModel.Skeleton(
                                        type = type,
                                    )
                                )

                                is TransactionInfo.Type.Transfer -> TransactionTypeModel.Skeleton.Transfer(
                                    skeleton = TransferModel.Skeleton(
                                        type
                                    )
                                )
                            }
                        }
                        .toMutableStateFlowAsInitial(),
                )
            }

            fun createForNew(
                transactionType: TransactionType,
            ): Skeleton {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                return Skeleton(
                    id = null,
                    comment = EditingString().toMutableStateFlowAsInitial(),
                    date = now.date.toMutableStateFlowAsInitial(),
                    time = now.time.toMutableStateFlowAsInitial(),
                    type = when (transactionType) {
                        TransactionType.Entry -> TransactionTypeModel.Skeleton.Entry(
                            skeleton = EntryModel.Skeleton.empty
                        )

                        TransactionType.Transfer -> TransactionTypeModel.Skeleton.Transfer(
                            skeleton = TransferModel.Skeleton.empty
                        )
                    }.toMutableStateFlowAsInitial(),
                )
            }
        }
    }

    val isNewTransaction: Boolean
        get() = skeleton.id == null

    val comment: MutableStateFlow<EditingString>
        get() = skeleton.comment

    val date: MutableStateFlow<LocalDate>
        get() = skeleton.date

    val time: MutableStateFlow<LocalTime>
        get() = skeleton.time

    @Shuffle
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun entry(): EntryModel.Dependencies

        fun transfer(): TransferModel.Dependencies
    }

    val type: StateFlow<TransactionTypeModel> = skeleton
        .type
        .mapWithScope(
            scope = scope,
        ) { typeScope, typeSkeleton ->
            when (typeSkeleton) {
                is TransactionTypeModel.Skeleton.Entry -> TransactionTypeModel.Entry(
                    model = EntryModel(
                        scope = typeScope,
                        skeleton = typeSkeleton.skeleton,
                        dependencies = dependencies.entry(),
                    )
                )

                is TransactionTypeModel.Skeleton.Transfer -> TransactionTypeModel.Transfer(
                    model = TransferModel(
                        scope = typeScope,
                        skeleton = typeSkeleton.skeleton,
                        dependencies = dependencies.transfer(),
                    )
                )
            }
        }

    val typeVariant: StateFlow<TransactionType> = type.mapState(
        scope = scope,
    ) { transactionTypeModel ->
        transactionTypeModel.type
    }

    fun chooseType(
        type: TransactionType,
    ) {
        if (skeleton.type.value.type == type) {
            return
        }
        skeleton.type.value = when (type) {
            TransactionType.Entry -> TransactionTypeModel.Skeleton.Entry(
                skeleton = EntryModel.Skeleton.empty,
            )

            TransactionType.Transfer -> TransactionTypeModel.Skeleton.Transfer(
                skeleton = TransferModel.Skeleton.empty,
            )
        }
    }

    private val result: StateFlow<Transaction?> = type
        .flatMapState(
            scope = scope,
        ) { typeModel ->
            typeModel.result
        }
        .combineStateWith(
            scope = scope,
            other = combineState(
                scope = scope,
                a = skeleton.date,
                b = skeleton.time,
            ) { date, time ->
                LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault())
            },
        ) { typeOrNull, timestamp ->
            typeOrNull?.let { type ->
                type to timestamp
            }
        }
        .combineStateWith(
            scope = scope,
            other = skeleton.comment,
        ) { typeWithTimestampOrNull, comment ->
            typeWithTimestampOrNull?.let { (type, timestamp) ->
                Transaction(
                    timestamp = timestamp,
                    type = type,
                    comment = comment.text.let(::Comment),
                )
            }
        }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> = result.mapWithScope(
        scope = scope,
    ) { transactionScope, transactionOrNull ->
        transactionOrNull?.let { transaction ->
            actionOrNullIfExecuting(
                scope = transactionScope,
            ) {
                dependencies.budgetRepository.transactions.addOrUpdate(
                    id = skeleton.id,
                    transaction = transaction,
                )
                completed()
            }
        }
    }

    val remove: StateFlow<(() -> Unit)?>? = skeleton
        .id
        ?.let { id ->
            actionOrNullIfExecuting(
                scope = scope,
            ) {
                dependencies.budgetRepository.transactions.remove(
                    id = skeleton.id,
                )
                completed()
            }
        }
}