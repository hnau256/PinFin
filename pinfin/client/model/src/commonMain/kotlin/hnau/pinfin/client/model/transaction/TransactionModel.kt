@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.transaction

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
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.model.transaction.type.TransactionTypeModel
import hnau.pinfin.client.model.transaction.type.entry.EntryModel
import hnau.pinfin.client.model.transaction.type.transfer.TransferModel
import hnau.pinfin.scheme.Comment
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.TransactionType
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
        var content: Content? = null,
    ) {

        @Serializable
        data class Content(
            val comment: MutableStateFlow<EditingString>,
            val date: MutableStateFlow<LocalDate>,
            val time: MutableStateFlow<LocalTime>,
            val type: MutableStateFlow<TransactionTypeModel.Skeleton>,
        )
    }

    private fun getSkeletonContent(): Skeleton.Content = skeleton::content
        .toAccessor()
        .getOrInit {
            when (val id = skeleton.id) {
                null -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    Skeleton.Content(
                        comment = EditingString().toMutableStateFlowAsInitial(),
                        date = now.date.toMutableStateFlowAsInitial(),
                        time = now.time.toMutableStateFlowAsInitial(),
                        type = when (TransactionType.default) {
                            TransactionType.Entry -> TransactionTypeModel.Skeleton.Entry(
                                skeleton = EntryModel.Skeleton.empty
                            )

                            TransactionType.Transfer -> TransactionTypeModel.Skeleton.Transfer(
                                skeleton = TransferModel.Skeleton.empty
                            )
                        }.toMutableStateFlowAsInitial(),
                    )
                }

                else -> {
                    val transaction = dependencies
                        .budgetRepository
                        .transaction
                        .map
                        .value
                        .getValue(id)
                    val localDateTime =
                        transaction.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                    Skeleton.Content(
                        comment = transaction.comment.text
                            .toEditingString()
                            .toMutableStateFlowAsInitial(),
                        date = localDateTime.date.toMutableStateFlowAsInitial(),
                        time = localDateTime.time.toMutableStateFlowAsInitial(),
                        type = transaction
                            .type
                            .let { type ->
                                when (type) {
                                    is Transaction.Type.Entry -> TransactionTypeModel.Skeleton.Entry(
                                        skeleton = EntryModel.Skeleton(
                                            type = type,
                                        )
                                    )

                                    is Transaction.Type.Transfer -> TransactionTypeModel.Skeleton.Transfer(
                                        skeleton = TransferModel.Skeleton(
                                            type
                                        )
                                    )
                                }
                            }
                            .toMutableStateFlowAsInitial(),
                    )
                }
            }
        }

    val isNewTransaction: Boolean
        get() = skeleton.id == null

    val comment: MutableStateFlow<EditingString>
        get() = getSkeletonContent().comment

    val date: MutableStateFlow<LocalDate>
        get() = getSkeletonContent().date

    val time: MutableStateFlow<LocalTime>
        get() = getSkeletonContent().time

    @Shuffle
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun entry(): EntryModel.Dependencies

        fun transfer(): TransferModel.Dependencies
    }

    val type: StateFlow<TransactionTypeModel> = getSkeletonContent()
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
        val skeletonContent = getSkeletonContent()
        if (skeletonContent.type.value.type == type) {
            return
        }
        skeletonContent.type.value = when (type) {
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
            other = getSkeletonContent().let { skeletonContent ->
                combineState(
                    scope = scope,
                    a = skeletonContent.date,
                    b = skeletonContent.time,
                ) { date, time ->
                    LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault())
                }
            },
        ) { typeOrNull, timestamp ->
            typeOrNull?.let { type ->
                type to timestamp
            }
        }
        .combineStateWith(
            scope = scope,
            other = getSkeletonContent().comment,
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
                dependencies.budgetRepository.transaction.addOrUpdate(
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
                dependencies.budgetRepository.transaction.remove(
                    id = skeleton.id,
                )
                completed()
            }
        }
}