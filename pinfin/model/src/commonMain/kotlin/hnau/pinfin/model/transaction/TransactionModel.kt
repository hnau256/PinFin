@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction

import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.toEditingString
import hnau.pinfin.data.Comment
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.type.TransactionTypeModel
import hnau.pinfin.model.transaction.type.entry.EntryModel
import hnau.pinfin.model.transaction.type.transfer.TransferModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
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
    private val onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val id: Transaction.Id?,
        val comment: MutableStateFlow<EditingString>,
        val date: MutableStateFlow<LocalDate>,
        val time: MutableStateFlow<LocalTime>,
        val type: MutableStateFlow<TransactionTypeModel.Skeleton>,
        val visibleDialog: MutableStateFlow<Dialog?> =
            null.toMutableStateFlowAsInitial(),
        val mainContent: MutableStateFlow<MainContent> =
            MainContent.Config.toMutableStateFlowAsInitial(),
    ) {

        enum class MainContent { Config, Date, Time }

        enum class Dialog { ExitUnsaved, Remove }

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

    private val inProgressRegistry = InProgressRegistry()

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    val isNewTransaction: Boolean
        get() = skeleton.id == null


    sealed interface MainContent {

        data class Config(
            val comment: MutableStateFlow<EditingString>,
            val date: StateFlow<LocalDate>,
            val time: StateFlow<LocalTime>,
            val chooseDate: () -> Unit,
            val chooseTime: () -> Unit,
            val typeVariant: StateFlow<TransactionType>,
            val chooseType: (TransactionType) -> Unit,
        ) : MainContent

        data class Date(
            val initialDate: LocalDate,
            val save: (LocalDate) -> Unit,
            val cancel: () -> Unit,
        ) : MainContent

        data class Time(
            val initialTime: LocalTime,
            val save: (LocalTime) -> Unit,
            val cancel: () -> Unit,
        ) : MainContent
    }



    @Pipe
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

    val mainContent: StateFlow<MainContent> = run {
        val switch: (Skeleton.MainContent) -> Unit = { skeleton.mainContent.value = it }
        val switchToConfig = { switch(Skeleton.MainContent.Config) }
        skeleton
            .mainContent
            .mapWithScope(scope) { stateScope, mainContent ->
                when (mainContent) {

                    Skeleton.MainContent.Config -> MainContent.Config(
                        comment = skeleton.comment,
                        date = skeleton.date,
                        time = skeleton.time,
                        chooseDate = { switch(Skeleton.MainContent.Date) },
                        chooseTime = { switch(Skeleton.MainContent.Time) },
                        typeVariant = type.mapState(
                            scope = stateScope,
                        ) { transactionTypeModel ->
                            transactionTypeModel.type
                        },
                        chooseType = { type ->
                            if (skeleton.type.value.type == type) {
                                return@Config
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
                    )

                    Skeleton.MainContent.Date -> MainContent.Date(
                        initialDate = skeleton.date.value,
                        save = { newDate ->
                            skeleton.date.value = newDate
                            switchToConfig()
                        },
                        cancel = switchToConfig,
                    )

                    Skeleton.MainContent.Time -> MainContent.Time(
                        initialTime = skeleton.time.value,
                        save = { newTime ->
                            skeleton.time.value = newTime
                            switchToConfig()
                        },
                        cancel = switchToConfig,
                    )
                }
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

    private val saveAction: StateFlow<(suspend () -> Unit)?> = result.mapWithScope(
        scope = scope,
    ) { transactionScope, transactionOrNull ->
        transactionOrNull?.let { transaction ->
            {
                dependencies.budgetRepository.transactions.addOrUpdate(
                    id = skeleton.id,
                    transaction = transaction,
                )
                onReady()
            }
        }
    }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> =
        saveAction.mapWithScope(scope) { saveScope, saveOrNull ->
            saveOrNull?.let { save ->
                actionOrNullIfExecuting(
                    scope = saveScope,
                    action = save,
                )
            }
        }

    val remove: (() -> Unit)? = skeleton.id?.let { id ->
        { skeleton.visibleDialog.value = Skeleton.Dialog.Remove }
    }

    private fun closeAnyDialog() {
        skeleton.visibleDialog.value = null
    }

    data class RemoveDialogInfo(
        val dismiss: () -> Unit,
        val remove: () -> Unit,
    )

    val removeDialogInfo: StateFlow<RemoveDialogInfo?> = skeleton.id.foldNullable(
        ifNull = { null.toMutableStateFlowAsInitial() },
        ifNotNull = { id ->
            skeleton.visibleDialog.mapState(scope) { dialog ->
                when (dialog) {
                    Skeleton.Dialog.Remove -> RemoveDialogInfo(
                        dismiss = ::closeAnyDialog,
                        remove = {
                            scope.launch {
                                inProgressRegistry.executeRegistered {
                                    dependencies.budgetRepository.transactions.remove(id)
                                    onReady()
                                }
                            }
                        }
                    )

                    else -> null
                }
            }
        }
    )

    data class ExitUnsavedDialogInfo(
        val dismiss: () -> Unit,
        val exitWithoutSaving: () -> Unit,
        val save: (() -> Unit)?,
    )

    val exitUnsavedDialogInfo: StateFlow<ExitUnsavedDialogInfo?> = skeleton
        .visibleDialog
        .scopedInState(scope)
        .flatMapState(scope) { (dialogScope, visibleDialog) ->
            when (visibleDialog) {
                Skeleton.Dialog.ExitUnsaved -> saveAction.mapState(dialogScope) { saveOrNull ->
                    ExitUnsavedDialogInfo(
                        dismiss = ::closeAnyDialog,
                        exitWithoutSaving = onReady,
                        save = saveOrNull?.let { save ->
                            {
                                scope.launch {
                                    inProgressRegistry.executeRegistered {
                                        save()
                                    }
                                }
                            }
                        },
                    )
                }

                else -> null.toMutableStateFlowAsInitial()
            }
        }

    override val goBackHandler: GoBackHandler = skeleton
        .visibleDialog
        .mapState(scope) { visibleDialog ->
            visibleDialog.foldNullable(
                ifNotNull = { { closeAnyDialog() } },
                ifNull = { { skeleton.visibleDialog.value = Skeleton.Dialog.ExitUnsaved } },
            )
        }
}