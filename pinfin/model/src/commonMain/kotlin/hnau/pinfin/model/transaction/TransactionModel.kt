@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.pageable.CommentModel
import hnau.pinfin.model.transaction.pageable.DateModel
import hnau.pinfin.model.transaction.pageable.TimeModel
import hnau.pinfin.model.transaction.pageable.TypeModel
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.combineEditableWith
import hnau.pinfin.model.transaction.utils.toTransactionType
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransactionModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onReady: () -> Unit,
) {

    enum class Part {

        Date, Time, Comment, Type;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    sealed interface PageType {

        val key: Int

        val goBackHandler: GoBackHandler

        data class Type(
            val model: TypeModel.Page,
        ) : PageType {
            override val key: Int
                get() = 0

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Date(
            val model: DateModel.Page,
        ) : PageType {
            override val key: Int
                get() = 1

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Time(
            val model: TimeModel.Page,
        ) : PageType {
            override val key: Int
                get() = 2

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Comment(
            val model: CommentModel.Page,
        ) : PageType {
            override val key: Int
                get() = 3

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun type(): TypeModel.Dependencies

        fun date(): DateModel.Dependencies

        fun time(): TimeModel.Dependencies

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
            false.toMutableStateFlowAsInitial()
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
                transaction: TransactionInfo,
            ): Skeleton {
                val timestamp = transaction
                    .timestamp
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                return Skeleton(
                    id = transaction.id,
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
        dependencies = dependencies.date(),
        skeleton = skeleton.date,
        isFocused = isPartFocused(Part.Date),
        requestFocus = createRequestFocus(Part.Date),
        goForward = createGoForward(Part.Date),
    )

    val time = TimeModel(
        scope = scope,
        dependencies = dependencies.time(),
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
            state.transactions.mapNotNull { transaction ->
                transaction
                    .comment
                    .takeIf { comment -> comment.text.isNotEmpty() }
                    ?.let { comment -> comment to transaction.timestamp }
            }
        },
        goForward = createGoForward(Part.Comment),
    )

    val pageType: StateFlow<Pair<Part, PageType>> = skeleton
        .part
        .mapWithScope(scope) { pageScope, part ->
            val pageType = when (part) {
                Part.Type -> PageType.Type(
                    model = type.createPage(
                        scope = pageScope,
                    ),
                )

                Part.Date -> PageType.Date(
                    model = date.createPage(
                        scope = pageScope,
                    ),
                )

                Part.Time -> PageType.Time(
                    model = time.createPage(
                        scope = pageScope,
                    ),
                )

                Part.Comment -> PageType.Comment(
                    model = comment.createPage(
                        scope = pageScope,
                    ),
                )
            }

            part to pageType
        }

    internal sealed interface State {

        data object NoChanges : State

        data class HasChanges(
            val saveIsCorrect: (() -> Unit)?,
            val closeWithoutSavingDialogInfo: CloseWithoutSavingDialogInfo?,
        ) : State {

            data class CloseWithoutSavingDialogInfo(
                val close: () -> Unit,
                val cancelChanges: () -> Unit,
            )
        }

        data object Saving : State
    }

    private fun createHasChangesState(
        scope: CoroutineScope,
        save: (() -> Unit)?
    ): StateFlow<State.HasChanges> = skeleton
        .closeWithoutSavingDialogIsVisible
        .mapState(scope) { closeWithoutSavingDialogIsVisible ->
            State.HasChanges(
                saveIsCorrect = save,
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

    private val state: StateFlow<State> = date.dateEditable
        .combineEditableWith(
            scope = scope,
            other = time.timeEditable,
        ) { date, time ->
            date
                .atTime(time)
                .toInstant(TimeZone.currentSystemDefault())
        }
        .combineEditableWith(
            scope = scope,
            other = comment.commentEditable,
            combine = ::Pair,
        )
        .combineEditableWith(
            scope = scope,
            other = type.type,
        ) { (timestamp, comment), type ->
            Transaction(
                type = type.toTransactionType(),
                timestamp = timestamp,
                comment = comment,
            )
        }
        .scopedInState(scope)
        .flatMapState(scope) { (scope, transactionOrIncorrect) ->
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
                            actionOrNullIfExecuting(scope) {
                                dependencies.budgetRepository.transactions.addOrUpdate(
                                    id = skeleton.id,
                                    transaction = transactionOrIncorrect.value,
                                )
                                onReady()
                            }
                                .scopedInState(scope)
                                .flatMapState(scope) { (scope, saveOrSaving) ->
                                    saveOrSaving.foldNullable(
                                        ifNull = { State.Saving.toMutableStateFlowAsInitial() },
                                        ifNotNull = { save ->
                                            createHasChangesState(
                                                scope = scope,
                                                save = save,
                                            )
                                        }
                                    )
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

    private fun createLocalGoBackHandler(
        scope: CoroutineScope,
    ): GoBackHandler = state.mapState(scope) { state ->
        when (state) {
            State.NoChanges -> null
            State.Saving -> {
                {}
            }

            is State.HasChanges -> state.closeWithoutSavingDialogInfo.foldNullable(
                ifNull = { { setCloseWithoutSavingDialogIsVisible(true) } },
                ifNotNull = { { setCloseWithoutSavingDialogIsVisible(false) } },
            )
        }
    }

    val goBackHandler: GoBackHandler = pageType
        .scopedInState(scope)
        .flatMapState(scope) { (pageScope, partWithPage) ->
            val (part, pageModel) = partWithPage
            pageModel.goBackHandler
                .scopedInState(pageScope)
                .flatMapState(pageScope) { (scope, goBack) ->
                    goBack.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            when (part) {
                                Part.Type -> type.goBackHandler
                                Part.Date -> date.goBackHandler
                                Part.Time -> time.goBackHandler
                                Part.Comment -> comment.goBackHandler
                            }
                                .scopedInState(scope)
                                .flatMapState(scope) { (scope, partGoBackOrNull) ->
                                    partGoBackOrNull.foldNullable(
                                        ifNotNull = { partGoBack ->
                                            partGoBack.toMutableStateFlowAsInitial()
                                        },
                                        ifNull = {
                                            part
                                                .shift(-1)
                                                .foldNullable(
                                                    ifNotNull = { previousPart ->
                                                        { switchToPart(previousPart) }.toMutableStateFlowAsInitial()
                                                    },
                                                    ifNull = { createLocalGoBackHandler(scope) }
                                                )
                                        }
                                    )
                                }
                        },
                    )
                }
        }
}