@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.page.PageModel
import hnau.pinfin.model.transaction.page.TypePageModel
import hnau.pinfin.model.transaction.part.type.EntryModel
import hnau.pinfin.model.transaction.part.type.PartTypeModel
import hnau.pinfin.model.transaction.part.type.TransferModel
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TypeModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) : PartModel {

    @Pipe
    interface Dependencies {

        fun entry(): EntryModel.Dependencies

        fun transfer(): TransferModel.Dependencies

        fun page(): TypePageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val type: MutableStateFlow<PartTypeModel.Skeleton>,
        var page: TypePageModel.Skeleton? = null,
    ) {

        companion object {

            fun createTypeForNew(
                type: TransactionType,
            ): PartTypeModel.Skeleton = when (type) {
                TransactionType.Entry -> EntryModel.Skeleton.createForNew()
                TransactionType.Transfer -> TransferModel.Skeleton.createForNew()
            }

            fun createForNew(
                type: TransactionType,
            ): Skeleton = Skeleton(
                type = createTypeForNew(type).toMutableStateFlowAsInitial()
            )

            fun createForEdit(
                type: TransactionInfo.Type,
            ): Skeleton = Skeleton(
                type = when (type) {
                    is TransactionInfo.Type.Entry -> EntryModel.Skeleton.createForEdit(
                        type = type,
                    )

                    is TransactionInfo.Type.Transfer -> TransferModel.Skeleton.createForEdit(
                        type = type,
                    )
                }.toMutableStateFlowAsInitial()
            )
        }
    }

    private val PartTypeModel.Skeleton.variant: TransactionType
        get() = when (this) {
            is EntryModel.Skeleton -> TransactionType.Entry
            is TransferModel.Skeleton -> TransactionType.Transfer
        }

    val type: StateFlow<PartTypeModel> = skeleton
        .type
        .mapWithScope(scope) { typeScope, skeleton ->
            when (skeleton) {
                is EntryModel.Skeleton -> EntryModel(
                    scope = typeScope,
                    dependencies = dependencies.entry(),
                    skeleton = skeleton,
                    requestFocus = requestFocus,
                    isFocused = isFocused,
                )

                is TransferModel.Skeleton -> TransferModel(
                    scope = typeScope,
                    dependencies = dependencies.transfer(),
                    skeleton = skeleton,
                    requestFocus = requestFocus,
                    isFocused = isFocused,
                )
            }
        }

    val typeVariant: StateFlow<TransactionType> =
        skeleton.type.mapStateLite { type -> type.variant }

    fun setVariant(
        type: TransactionType,
    ) {
        skeleton
            .type
            .update { current ->
                current
                    .takeIf { it.variant == type }
                    .ifNull { Skeleton.createTypeForNew(type) }
            }
    }

    override fun createPage(
        scope: CoroutineScope,
        navAction: NavAction
    ): PageModel = TypePageModel(
        scope = scope,
        dependencies = dependencies.page(),
        navAction = navAction,
        type = type,
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { TypePageModel.Skeleton() },
    )
}