@file:UseSerializers(
    MutableStateFlowSerializer::class,
    EitherSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.serialization.EitherSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapMutableState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TypeModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val isFocused: StateFlow<Boolean>,
    private val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun entry(): EntryModel.Dependencies

        fun transfer(): TransferModel.Dependencies
    }

    sealed interface Type {

        val key: TransactionType
            get() = when (this) {
                is Entry -> TransactionType.Entry
                is Transfer -> TransactionType.Transfer
            }

        val goBackHandler: GoBackHandler

        data class Entry(
            val model: EntryModel,
        ) : Type {

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Transfer(
            val model: TransferModel,
        ) : Type {

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        @Serializable
        sealed interface Skeleton {

            val key: TransactionType
                get() = when (this) {
                    is Entry -> TransactionType.Entry
                    is Transfer -> TransactionType.Transfer
                }

            @Serializable
            @SerialName("entry")
            data class Entry(
                val skeleton: EntryModel.Skeleton,
            ) : Skeleton

            @Serializable
            @SerialName("transfer")
            data class Transfer(
                val skeleton: TransferModel.Skeleton,
            ) : Skeleton
        }
    }

    @Serializable
    data class Skeleton(
        val type: MutableStateFlow<Type.Skeleton>,
    ) {

        companion object {

            fun createType(
                type: TransactionType,
            ): Type.Skeleton = when (type) {
                TransactionType.Entry -> Type.Skeleton.Entry(
                    skeleton = EntryModel.Skeleton.createForNew(),
                )

                TransactionType.Transfer -> Type.Skeleton.Transfer(
                    skeleton = TransferModel.Skeleton.createForNew(),
                )
            }

            fun createForNew(
                type: TransactionType,
            ): Skeleton = Skeleton(
                type = createType(
                    type = type,
                ).toMutableStateFlowAsInitial()
            )

            fun createForEdit(
                type: TransactionInfo.Type,
            ): Skeleton = Skeleton(
                type = when (type) {
                    is TransactionInfo.Type.Entry -> Type.Skeleton.Entry(
                        skeleton = EntryModel.Skeleton.createForEdit(
                            entry = type,
                        ),
                    )

                    is TransactionInfo.Type.Transfer -> Type.Skeleton.Transfer(
                        skeleton = TransferModel.Skeleton.createForEdit(
                            transfer = type,
                        ),
                    )
                }.toMutableStateFlowAsInitial()
            )
        }
    }

    val variant: MutableStateFlow<TransactionType> = skeleton
        .type
        .mapMutableState(
            scope = scope,
            mapper = Mapper(
                direct = Type.Skeleton::key,
                reverse = Skeleton.Companion::createType,
            )
        )

    val typeModel: StateFlow<Type> = skeleton
        .type
        .mapWithScope(scope) { typeScope, skeleton ->
            when (skeleton) {
                is Type.Skeleton.Entry -> Type.Entry(
                    model = EntryModel(
                        scope = typeScope,
                        dependencies = dependencies.entry(),
                        skeleton = skeleton.skeleton,
                        isFocused = isFocused,
                        requestFocus = requestFocus,
                        goForward = goForward,
                    )
                )

                is Type.Skeleton.Transfer -> Type.Transfer(
                    model = TransferModel(
                        scope = typeScope,
                        dependencies = dependencies.transfer(),
                        skeleton = skeleton.skeleton,
                        isFocused = isFocused,
                        requestFocus = requestFocus,
                        goForward = goForward,
                    )
                )
            }
        }

    class Page(
        scope: CoroutineScope,
        val page: StateFlow<Type>,
    ) {

        sealed interface Type {

            val key: TransactionType
                get() = when (this) {
                    is Entry -> TransactionType.Entry
                    is Transfer -> TransactionType.Transfer
                }

            val goBackHandler: GoBackHandler

            data class Entry(
                val model: EntryModel.Page,
            ) : Type {

                override val goBackHandler: GoBackHandler
                    get() = model.goBackHandler
            }

            data class Transfer(
                val model: TransferModel.Page,
            ) : Type {

                override val goBackHandler: GoBackHandler
                    get() = model.goBackHandler
            }
        }

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, Type::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        page = typeModel.mapWithScope(scope) { typeScope, typeModel ->
            when (typeModel) {
                is Type.Entry -> Page.Type.Entry(
                    model = typeModel.model.createPage(
                        scope = typeScope,
                    )
                )

                is Type.Transfer -> Page.Type.Transfer(
                    model = typeModel.model.createPage(
                        scope = typeScope,
                    )
                )
            }
        }
    )

    internal val type: StateFlow<Editable<TransactionInfo.Type>> = typeModel.flatMapState(scope) { typeModel ->
        when (typeModel) {
            is Type.Entry -> typeModel.model.entry
            is Type.Transfer -> typeModel.model.transfer
        }
    }

    val goBackHandler: GoBackHandler =
        typeModel.flatMapState(scope, Type::goBackHandler)
}