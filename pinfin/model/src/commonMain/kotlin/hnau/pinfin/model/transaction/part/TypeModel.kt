@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapStateLite
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
import hnau.pinfin.model.transaction.part.type.TransferModel
import hnau.pinfin.model.transaction.part.type.TypePartModel
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
) {

    @Pipe
    interface Dependencies {

        fun entry(): EntryModel.Dependencies

        fun transfer(): TransferModel.Dependencies

        fun page(): TypePageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val type: MutableStateFlow<TypePartModel.Skeleton>,
        var page: TypePageModel.Skeleton? = null,
    ) {

        companion object {

            fun createTypeForNew(
                type: TransactionType,
            ): TypePartModel.Skeleton = when (type) {
                TransactionType.Entry -> TypePartModel.Skeleton.Entry(
                    skeleton = EntryModel.Skeleton.createForNew(),
                )

                TransactionType.Transfer -> TypePartModel.Skeleton.Transfer(
                    skeleton = TransferModel.Skeleton.createForNew(),
                )
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
                    is TransactionInfo.Type.Entry -> TypePartModel.Skeleton.Entry(
                        skeleton = EntryModel.Skeleton.createForEdit(
                            type = type,
                        ),
                    )

                    is TransactionInfo.Type.Transfer -> TypePartModel.Skeleton.Transfer(
                        skeleton = TransferModel.Skeleton.createForEdit(
                            type = type,
                        ),
                    )
                }.toMutableStateFlowAsInitial()
            )
        }
    }

    private val TypePartModel.Skeleton.variant: TransactionType
        get() = when (this) {
            is TypePartModel.Skeleton.Entry -> TransactionType.Entry
            is TypePartModel.Skeleton.Transfer -> TransactionType.Transfer
        }

    val type: StateFlow<TypePartModel> = skeleton
        .type
        .mapWithScope(scope) { typeScope, skeleton ->
            when (skeleton) {
                is TypePartModel.Skeleton.Entry -> TypePartModel.Entry(
                    model = EntryModel(
                        scope = typeScope,
                        dependencies = dependencies.entry(),
                        skeleton = skeleton.skeleton,
                        requestFocus = requestFocus,
                        isFocused = isFocused,
                    ),
                )

                is TypePartModel.Skeleton.Transfer -> TypePartModel.Transfer(
                    model = TransferModel(
                        scope = typeScope,
                        dependencies = dependencies.transfer(),
                        skeleton = skeleton.skeleton,
                        requestFocus = requestFocus,
                        isFocused = isFocused,
                    ),
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

    fun createPage(
        scope: CoroutineScope,
    ): PageModel = PageModel.Type(
        model = TypePageModel(
            scope = scope,
            dependencies = dependencies.page(),
            type = type,
            skeleton = skeleton::page
                .toAccessor()
                .getOrInit { TypePageModel.Skeleton() },
        ),
    )

    val goBackHandler: GoBackHandler =
        type.flatMapStateLite(TypePartModel::goBackHandler)
}