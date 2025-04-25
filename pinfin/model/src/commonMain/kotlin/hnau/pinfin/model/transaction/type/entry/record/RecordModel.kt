@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.entry.record

import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.app.toEditingString
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.repository.CategoryInfo
import hnau.pinfin.repository.TransactionInfo
import hnau.pinfin.model.AmountModel
import hnau.pinfin.data.Comment
import hnau.pinfin.data.Record
import hnau.pinfin.model.transaction.type.utils.ChooseCategoryModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val remove: StateFlow<(() -> Unit)?>,
    localUsedCategories: StateFlow<Set<CategoryInfo>>,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val category: MutableStateFlow<CategoryInfo?>,
        val amount: AmountModel.Skeleton,
        val comment: MutableStateFlow<EditingString>,
        val overlapDialog: MutableStateFlow<OverlapDialog?> = null.toMutableStateFlowAsInitial(),
    ) {

        constructor(
            record: TransactionInfo.Type.Entry.Record,
        ) : this(
            category = record.category.toMutableStateFlowAsInitial(),
            amount = AmountModel.Skeleton(
                amount = record.amount,
            ),
            comment = record
                .comment
                .text
                .toEditingString()
                .toMutableStateFlowAsInitial(),
        )

        @Serializable
        sealed interface OverlapDialog {

            @Serializable
            @SerialName("remove")
            data object Remove : OverlapDialog

            @Serializable
            @SerialName("choose_category")
            data class ChooseCategory(
                val chooseCategorySkeleton: ChooseCategoryModel.Skeleton,
            ) : OverlapDialog
        }

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    category = null.toMutableStateFlowAsInitial(),
                    amount = AmountModel.Skeleton.empty,
                    comment = "".toEditingString().toMutableStateFlowAsInitial(),
                )
        }
    }

    @Shuffle
    interface Dependencies {

        fun amount(): AmountModel.Dependencies

        fun chooseCategory(): ChooseCategoryModel.Dependencies
    }

    val category: StateFlow<CategoryInfo?>
        get() = skeleton.category

    fun openCategoryChooser() {
        skeleton.overlapDialog.value = Skeleton.OverlapDialog.ChooseCategory(
            chooseCategorySkeleton = ChooseCategoryModel.Skeleton.empty
        )
    }

    val openRemoveOverlap: StateFlow<(() -> Unit)?> = remove.mapState(
        scope = scope,
    ) { removeOrNull ->
        removeOrNull?.let {
            { skeleton.overlapDialog.value = Skeleton.OverlapDialog.Remove }
        }
    }

    val amount = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
    )

    sealed interface OverlapDialogModel {

        data class Remove(
            val remove: () -> Unit,
        ) : OverlapDialogModel

        data class ChooseCategory(
            val chooseCategoryModel: ChooseCategoryModel,
        ) : OverlapDialogModel
    }

    fun closeOverlap() {
        skeleton.overlapDialog.value = null
    }

    val overlap: StateFlow<OverlapDialogModel?> = skeleton
        .overlapDialog
        .scopedInState(
            parentScope = scope,
        )
        .flatMapState(
            scope = scope,
        ) { (stateScope, dialogOrNull) ->
            when (dialogOrNull) {

                null -> null.toMutableStateFlowAsInitial()

                is Skeleton.OverlapDialog.ChooseCategory -> OverlapDialogModel.ChooseCategory(
                    ChooseCategoryModel(
                        scope = stateScope,
                        dependencies = dependencies.chooseCategory(),
                        skeleton = dialogOrNull.chooseCategorySkeleton,
                        selected = skeleton.category,
                        updateSelected = { skeleton.category.value = it },
                        onReady = ::closeOverlap,
                        localUsedCategories = localUsedCategories,
                    )
                ).toMutableStateFlowAsInitial()

                Skeleton.OverlapDialog.Remove -> remove.mapState(
                    scope = stateScope,
                ) { removeOrNull ->
                    removeOrNull?.let { remove ->
                        OverlapDialogModel.Remove(
                            remove = remove,
                        )
                    }
                }
            }
        }

    val comment: MutableStateFlow<EditingString>
        get() = skeleton.comment

    val record: StateFlow<Record?> = skeleton
        .category
        .combineStateWith(
            scope = scope,
            other = amount.amount,
        ) { categoryOrNull, amountOrNull ->
            categoryOrNull?.let { category ->
                amountOrNull?.let { amount ->
                    category to amount
                }
            }
        }
        .combineStateWith(
            scope = scope,
            other = skeleton.comment,
        ) { categoryAndAmountOrNull, comment ->
            val (category, amount) = categoryAndAmountOrNull ?: return@combineStateWith null
            Record(
                category = category.id,
                amount = amount,
                comment = comment.text.let(::Comment),
            )
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}