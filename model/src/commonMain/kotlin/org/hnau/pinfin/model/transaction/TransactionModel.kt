@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.transaction.pageable.CommentModel
import org.hnau.pinfin.model.transaction.pageable.DateModel
import org.hnau.pinfin.model.transaction.pageable.TimeModel
import org.hnau.pinfin.model.transaction.pageable.TypeModel
import org.hnau.pinfin.model.transaction.utils.toTransactionType
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

class TransactionModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onReady: () -> Unit,
) {

    @Fold
    enum class Part {

        Date, Time, Comment, Type;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    @SealUp(
        variants = [
            Variant(
                type = TypeModel.Page::class,
                identifier = "type",
            ),
            Variant(
                type = DateModel.Page::class,
                identifier = "date",
            ),
            Variant(
                type = TimeModel.Page::class,
                identifier = "time",
            ),
            Variant(
                type = CommentModel.Page::class,
                identifier = "comment",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "TransactionModelPageType",
    )
    interface PageType {

        val goBackHandler: GoBackHandler

        companion object
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun type(): TypeModel.Dependencies

        fun comment(): CommentModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val id: Transaction.Id?,
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val type: TypeModel.Skeleton,
        val date: DateModel.Skeleton,
        val time: TimeModel.Skeleton,
        val comment: CommentModel.Skeleton,
        val closeWithoutSavingDialogIsVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
        val removeDialogIsVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(), //TODO Use ModelSavableDelegate
    ) {

        companion object {

            fun createForNew(
                type: TransactionType,
            ): Skeleton = Skeleton(
                id = null,
                type = TypeModel.Skeleton.createForNew(
                    type = type,
                ),
                date = DateModel.Skeleton.createForNew(),
                time = TimeModel.Skeleton.createForNew(),
                comment = CommentModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                id: Transaction.Id,
                transaction: TransactionInfo,
            ): Skeleton {
                val timestamp = transaction
                    .timestamp
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                return Skeleton(
                    id = id,
                    type = TypeModel.Skeleton.createForEdit(
                        type = transaction.type,
                    ),
                    date = DateModel.Skeleton.createForEdit(
                        date = timestamp.date,
                    ),
                    time = TimeModel.Skeleton.createForEdit(
                        time = timestamp.time,
                    ),
                    comment = CommentModel.Skeleton.createForEdit(
                        comment = transaction.comment,
                    ),
                )
            }
        }
    }

    private fun switchToPart(
        part: Part,
    ) {
        skeleton.part.value = part
    }

    private fun createRequestFocus(
        part: Part,
    ): () -> Unit = { switchToPart(part) }

    private fun isPartFocused(
        part: Part,
    ): StateFlow<Boolean> = skeleton
        .part
        .mapState(scope) { it == part }

    private fun createGoForward(
        from: Part,
    ): () -> Unit = {
        from
            .shift(1)
            .foldNullable(
                ifNull = { /*TODO*/ },
                ifNotNull = skeleton.part::value::set,
            )
    }

    val type = TypeModel(
        scope = scope,
        dependencies = dependencies.type(),
        skeleton = skeleton.type,
        isFocused = isPartFocused(Part.Type),
        requestFocus = createRequestFocus(Part.Type),
        goForward = createGoForward(Part.Type),
    )

    val date = DateModel(
        scope = scope,
        skeleton = skeleton.date,
        isFocused = isPartFocused(Part.Date),
        requestFocus = createRequestFocus(Part.Date),
        goForward = createGoForward(Part.Date),
    )

    val time = TimeModel(
        scope = scope,
        skeleton = skeleton.time,
        isFocused = isPartFocused(Part.Time),
        requestFocus = createRequestFocus(Part.Time),
        goForward = createGoForward(Part.Time),
    )

    val comment = CommentModel(
        scope = scope,
        dependencies = dependencies.comment(),
        skeleton = skeleton.comment,
        isFocused = isPartFocused(Part.Comment),
        requestFocus = createRequestFocus(Part.Comment),
        extractSuggests = { state ->
            state.transactions.mapNotNull { idWithTransaction ->
                idWithTransaction
                    .value
                    .comment
                    .takeIf { comment ->
                        comment.text.isNotEmpty()
                    }
                    ?.let { comment ->
                        comment to idWithTransaction.value.timestamp
                    }
            }
        },
        goForward = createGoForward(Part.Comment),
    )

    val pageType: StateFlow<Pair<Part, TransactionModelPageType>> = skeleton
        .part
        .mapWithScope(scope) { scope, part ->
            val pageType = part.fold(
                ifType = {
                    PageType.type(
                        type.createPage(
                            scope = scope,
                        ),
                    )
                },
                ifDate = {
                    PageType.date(
                        date.createPage(),
                    )
                },
                ifTime = {
                    PageType.time(
                        time.createPage(),
                    )
                },
                ifComment = {
                    PageType.comment(
                        comment.createPage(
                            scope = scope,
                        ),
                    )
                },
            )

            part to pageType
        }

    internal sealed interface State {

        data object NoChanges : State

        data class HasChanges(
            val saveIfCorrect: (suspend (CoroutineScope) -> Unit)?,
            val closeWithoutSavingDialogInfo: CloseWithoutSavingDialogInfo?,
        ) : State {

            data class CloseWithoutSavingDialogInfo(
                val close: () -> Unit,
                val cancelChanges: () -> Unit,
            )
        }
    }

    private fun createHasChangesState(
        scope: CoroutineScope,
        save: (suspend (CoroutineScope) -> Unit)?
    ): StateFlow<State.HasChanges> = skeleton
        .closeWithoutSavingDialogIsVisible
        .mapState(scope) { closeWithoutSavingDialogIsVisible ->
            State.HasChanges(
                saveIfCorrect = save,
                closeWithoutSavingDialogInfo = closeWithoutSavingDialogIsVisible.ifTrue {
                    State.HasChanges.CloseWithoutSavingDialogInfo(
                        close = {
                            setCloseWithoutSavingDialogIsVisible(
                                visible = false,
                            )
                        },
                        cancelChanges = onReady,
                    )
                }
            )
        }

    private fun setCloseWithoutSavingDialogIsVisible(
        visible: Boolean,
    ) {
        skeleton.closeWithoutSavingDialogIsVisible.value = visible
    }

    private val state: StateFlow<State> = derivedStateFlowOf(scope) {
        editable {
            Transaction(
                type = type.type.state.bind().toTransactionType(),
                timestamp = date.dateEditable.state.bind()
                    .atTime(time.timeEditable.state.bind())
                    .toInstant(TimeZone.currentSystemDefault()),
                comment = comment.commentEditable.state.bind(),
            )
        }
    }.flatMapWithScope(scope) { scope, transactionOrIncorrect ->
        when (transactionOrIncorrect) {
            Editable.Incorrect -> createHasChangesState(
                scope = scope,
                save = null,
            )

            is Editable.Value<Transaction> -> transactionOrIncorrect
                .changed
                .foldBoolean(
                    ifFalse = { State.NoChanges.toMutableStateFlowAsInitial() },
                    ifTrue = {
                        createHasChangesState(
                            scope = scope,
                            save = {
                                dependencies.budgetRepository.transactions.addOrUpdate(
                                    id = skeleton.id,
                                    transaction = transactionOrIncorrect.value,
                                )
                                onReady()
                            }
                        )
                    }
                )
        }
        }

    val saveOrDisabled: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>?> =
        state.flatMapWithScope(scope) { scope, state ->
            val action: (suspend (CoroutineScope) -> Unit) = when (state) {
                State.NoChanges -> {
                    { onReady }
                }

                is State.HasChanges -> state.saveIfCorrect
            } ?: return@flatMapWithScope null.toMutableStateFlowAsInitial()
            actionOrCancelIfExecuting(
                scope = scope,
                operation = action,
            )
        }


    data class CancelDialogInfo(
        val close: () -> Unit,
        val cancelChanges: () -> Unit,
        val saveIfPossible: (suspend (CoroutineScope) -> Unit)?,
    )

    val cancelDialogInfo: StateFlow<CancelDialogInfo?> = state
        .mapState(scope) { state ->
            when (state) {
                State.NoChanges -> null
                is State.HasChanges -> state
                    .closeWithoutSavingDialogInfo
                    ?.let { info ->
                        CancelDialogInfo(
                            saveIfPossible = state.saveIfCorrect,
                            close = info.close,
                            cancelChanges = info.cancelChanges,
                        )
                    }
            }
        }

    val remove: (() -> Unit)? = skeleton.id?.let { id ->
        { skeleton.removeDialogIsVisible.value = true }
    }

    data class RemoveDialogInfo(
        val close: () -> Unit,
        val remove: () -> Unit,
    )

    private fun closeRemoveDialog() {
        skeleton.removeDialogIsVisible.value = false
    }

    val removeDialogInfo: StateFlow<RemoveDialogInfo?> = skeleton
        .removeDialogIsVisible
        .mapState(scope) { removeDialogIsVisible ->
            removeDialogIsVisible.ifTrue {
                RemoveDialogInfo(
                    close = ::closeRemoveDialog,
                    remove = {
                        scope.launch {
                            dependencies
                                .budgetRepository
                                .transactions
                                .remove(skeleton.id!!)
                            onReady()
                        }
                    }
                )
            }
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        val (part, pageModel) = pageType.state
        pageModel.goBackHandler.state
            ?: part
                .fold(
                    ifType = { type.goBackHandler },
                    ifDate = { date.goBackHandler },
                    ifTime = { time.goBackHandler },
                    ifComment = { comment.goBackHandler },
                )
                .state
            ?: skeleton.removeDialogIsVisible.state.foldBoolean(
                ifTrue = { ::closeRemoveDialog },
                ifFalse = {
                    when (val currentState = state.state) {
                        State.NoChanges -> null
                        is State.HasChanges -> currentState.closeWithoutSavingDialogInfo.foldNullable(
                            ifNull = { { setCloseWithoutSavingDialogIsVisible(true) } },
                            ifNotNull = { { setCloseWithoutSavingDialogIsVisible(false) } },
                        )
                    }
                }
            )
    }
}