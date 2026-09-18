@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.ModelSavableDelegate
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.data.records.RecordEntry
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.transaction.edit.utils.SelectedPartDelegate
import org.hnau.pinfin.model.transaction.utils.toRaw
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountIdWithInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo

class TransactionEditModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    onReady: () -> Unit,
) {

    enum class Part {

        Date,
        Comment,
        Type;

        companion object {

            val default: Part
                get() = Comment
        }

    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun comment(): CommentEditModel.Dependencies

        fun type(): TransactionTypeEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val id: Transaction.Id?,
        val date: DateChooseModel.Skeleton,
        val comment: CommentEditModel.Skeleton,
        val type: TransactionTypeEditModel.Skeleton,
        val selectedPart: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val saveableDelegate: ModelSavableDelegate.Skeleton<Transaction<AccountIdWithInfo, CategoryIdWithInfo, RecordEntry<CategoryIdWithInfo>>> = ModelSavableDelegate.Skeleton(),
    ) {

        companion object {

            fun createForNew(
                type: TransactionType = TransactionType.default,
            ): Skeleton = Skeleton(
                id = null,
                date = DateChooseModel.Skeleton.createForNew(),
                comment = CommentEditModel.Skeleton.createForNew(),
                type = TransactionTypeEditModel.Skeleton.createForNew(
                    type = type,
                ),
            )

            fun create(
                id: Transaction.Id,
                transaction: Transaction<AccountIdWithInfo, CategoryIdWithInfo, *>,
            ): Skeleton = Skeleton(
                id = id,
                date = DateChooseModel.Skeleton.createForEdit(
                    date = transaction.timestamp,
                ),
                comment = CommentEditModel.Skeleton.create(
                    initial = transaction.comment,
                ),
                type = TransactionTypeEditModel.Skeleton.create(
                    type = transaction.type,
                )
            )
        }
    }

    private val selectedPart: SelectedPartDelegate<Part> = SelectedPartDelegate.create(
        scope = scope,
        selectedPart = skeleton.selectedPart,
        onPartChanged = skeleton.selectedPart::value::set,
        navigateContext = EditNavigateContext(
            isFocused = true.toMutableStateFlowAsInitial(),
            requestFocus = {},
            goForward = {},
        ),
    )

    val date = DateChooseModel(
        scope = scope,
        skeleton = skeleton.date,
        navigateContext = selectedPart.createPartNavigateContext(Part.Date),
    )

    val comment = CommentEditModel(
        scope = scope,
        skeleton = skeleton.comment,
        dependencies = dependencies.comment(),
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
        navigateContext = selectedPart.createPartNavigateContext(Part.Comment),
    )

    val type = TransactionTypeEditModel(
        scope = scope,
        dependencies = dependencies.type(),
        skeleton = skeleton.type,
        navigateContext = selectedPart.createPartNavigateContext(Part.Type),
    )

    val currency: StateFlow<Currency> = dependencies
        .budgetRepository
        .state
        .mapState(scope) { state -> state.info.currency }

    val editableTransaction: StateFlow<Editable<Transaction<AccountIdWithInfo, CategoryIdWithInfo, RecordEntry<CategoryIdWithInfo>>>> =
        derivedStateFlowOf(scope) {
            editable {
                Transaction(
                    timestamp = date.dateEditable.state.bind(),
                    comment = comment.commentEditable.state.bind(),
                    type = type.typeEditable.state.bind(),
                )
            }
        }

    val goBackDelegate: ModelSavableDelegate<Transaction<AccountIdWithInfo, CategoryIdWithInfo, RecordEntry<CategoryIdWithInfo>>> = ModelSavableDelegate(
        scope = scope,
        result = editableTransaction,
        skeleton = skeleton.saveableDelegate,
        modelGoBackHandler = type.goBackHandler,
        close = onReady,
        save = { transactionToSave ->
            dependencies
                .budgetRepository
                .transactions
                .addOrUpdate(
                    id = skeleton.id,
                    transaction = transactionToSave.toRaw(),
                )
            onReady()
        },
    )


    val goBackHandler: GoBackHandler
        get() = goBackDelegate.goBackHandler
}