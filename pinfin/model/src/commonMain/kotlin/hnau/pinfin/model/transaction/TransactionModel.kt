@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.part.CommentModel
import hnau.pinfin.model.transaction.part.DateModel
import hnau.pinfin.model.transaction.part.PartModel
import hnau.pinfin.model.transaction.part.TimeModel
import hnau.pinfin.model.transaction.part.TypeModel
import hnau.pinfin.model.transaction.page.PageModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransactionModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val onReady: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun date(): DateModel.Dependencies

        fun time(): TimeModel.Dependencies

        fun comment(): CommentModel.Dependencies

        fun type(): TypeModel.Dependencies

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val id: Transaction.Id?,
        val selectedPart: MutableStateFlow<Part> = Part.Comment.toMutableStateFlowAsInitial(),
        val date: DateModel.Skeleton,
        val time: TimeModel.Skeleton,
        val comment: CommentModel.Skeleton,
        val type: TypeModel.Skeleton,
    ) {

        companion object {

            fun createForNew(
                transactionType: TransactionType,
            ): Skeleton = Skeleton(
                id = null,
                date = DateModel.Skeleton.createForNew(),
                time = TimeModel.Skeleton.createForNew(),
                comment = CommentModel.Skeleton.createForNew(),
                type = TypeModel.Skeleton.createForNew(
                    type = transactionType,
                ),
            )

            fun createForEdit(
                transactionInfo: TransactionInfo,
            ): Skeleton {
                val dateTime = transactionInfo
                    .timestamp
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                return Skeleton(
                    id = transactionInfo.id,
                    date = DateModel.Skeleton.createForEdit(
                        date = dateTime.date,
                    ),
                    time = TimeModel.Skeleton.createForEdit(
                        time = dateTime.time,
                    ),
                    comment = CommentModel.Skeleton.createForEdit(
                        comment = transactionInfo.comment,
                    ),
                    type = TypeModel.Skeleton.createForEdit(
                        type = transactionInfo.type,
                    )
                )
            }
        }
    }

    val isNewTransaction: Boolean
        get() = skeleton.id == null

    private fun switchToPart(
        part: Part,
    ) {
        skeleton.selectedPart.value = part
    }

    val date = DateModel(
        scope = scope,
        dependencies = dependencies.date(),
        skeleton = skeleton.date,
        requestFocus = { switchToPart(Part.Date) },
        isFocused = skeleton
            .selectedPart
            .mapState(scope) { it == Part.Date },
    )

    val time = TimeModel(
        scope = scope,
        dependencies = dependencies.time(),
        skeleton = skeleton.time,
        requestFocus = { switchToPart(Part.Time) },
        isFocused = skeleton
            .selectedPart
            .mapState(scope) { it == Part.Time },
    )

    val comment = CommentModel(
        scope = scope,
        dependencies = dependencies.comment(),
        skeleton = skeleton.comment,
        requestFocus = { switchToPart(Part.Comment) },
        isFocused = skeleton
            .selectedPart
            .mapState(scope) { it == Part.Comment },
    )

    val type = TypeModel(
        scope = scope,
        dependencies = dependencies.type(),
        skeleton = skeleton.type,
        requestFocus = { switchToPart(Part.Comment) },
        isFocused = skeleton
            .selectedPart
            .mapState(scope) { it == Part.Comment },
    )

    private val parts: PartValues<PartModel> = PartValues(
        date = date,
        time = time,
        comment = comment,
        type = type,
    )

    //TODO
    val result: StateFlow<Transaction?> = null.toMutableStateFlowAsInitial()

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

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val page: StateFlow<Pair<Part, PageModel>> = skeleton
        .selectedPart
        .mapWithScope(scope) { pageScope, part ->
           val model = parts[part].createPage(
                scope = pageScope,
                /*navAction = part
                    .shift(1)
                    .let { nextOrNull ->
                        nextOrNull.foldNullable(
                            ifNull = {
                                NavAction(
                                    type = NavAction.Type.Done,
                                    onClick = saveAction,
                                )
                            },
                            ifNotNull = { next ->
                                NavAction(
                                    type = NavAction.Type.Next,
                                    onClick = MutableStateFlow {
                                        switchToPart(next)
                                    },
                                )
                            }
                        )
                    }*/
            )
            part to model
        }

    val goBackHandler: GoBackHandler = page
        .scopedInState(scope)
        .flatMapState(scope) {(pageScope, partWithPage) ->
            val (part, pageModel) = partWithPage
            pageModel.goBackHandler
                .scopedInState(pageScope)
                .flatMapState(pageScope) {(goBackScope, goBack) ->
                    goBack.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            page.mapState(goBackScope) {(part, _) ->
                                part
                                    .shift(-1)
                                    ?.let {previousPart ->
                                        { skeleton.selectedPart.value = previousPart }
                                    }
                            }
                        },
                    )
                }
        }
}