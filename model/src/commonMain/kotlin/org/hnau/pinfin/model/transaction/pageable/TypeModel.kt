@file:UseSerializers(
    MutableStateFlowSerializer::class,
    EitherSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.serialization.EitherSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.mapMutableState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.state.foldRaw

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

    @Fold
    sealed interface Type {

        val key: TransactionType
            get() = fold(
                ifEntry = { _ -> TransactionType.Entry },
                ifTransfer = { _ -> TransactionType.Transfer },
            )

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

        @Fold
        @Serializable
        sealed interface Skeleton {

            val key: TransactionType
                get() = fold(
                    ifEntry = { _ -> TransactionType.Entry },
                    ifTransfer = { _ -> TransactionType.Transfer },
                )

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
            ): Type.Skeleton = type.fold(
                ifEntry = {
                    Type.Skeleton.Entry(
                        skeleton = EntryModel.Skeleton.createForNew(),
                    )
                },
                ifTransfer = {
                    Type.Skeleton.Transfer(
                        skeleton = TransferModel.Skeleton.createForNew(),
                    )
                },
            )

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
                type = type.foldRaw(
                    ifEntry = { variant ->
                        Type.Skeleton.Entry(
                            skeleton = EntryModel.Skeleton.createForEdit(
                                entry = variant,
                            ),
                        )
                    },
                    ifTransfer = { variant ->
                        Type.Skeleton.Transfer(
                            skeleton = TransferModel.Skeleton.createForEdit(
                                transfer = variant,
                            ),
                        )
                    },
                ).toMutableStateFlowAsInitial()
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
        .mapWithScope(scope) { scope, skeleton ->
            skeleton.fold(
                ifEntry = { entrySkeleton ->
                    Type.Entry(
                        model = EntryModel(
                            scope = scope,
                            dependencies = dependencies.entry(),
                            skeleton = entrySkeleton,
                            isFocused = isFocused,
                            requestFocus = requestFocus,
                            goForward = goForward,
                        )
                    )
                },
                ifTransfer = { transferSkeleton ->
                    Type.Transfer(
                        model = TransferModel(
                            scope = scope,
                            dependencies = dependencies.transfer(),
                            skeleton = transferSkeleton,
                            isFocused = isFocused,
                            requestFocus = requestFocus,
                            goForward = goForward,
                        )
                    )
                },
            )
        }

    class Page(
        scope: CoroutineScope,
        val page: StateFlow<Type>,
    ) {

        @Fold
        sealed interface Type {

            val key: TransactionType
                get() = fold(
                    ifEntry = { _ -> TransactionType.Entry },
                    ifTransfer = { _ -> TransactionType.Transfer },
                )

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
        page = typeModel.mapWithScope(scope) { scope, typeModel ->
            typeModel.fold(
                ifEntry = { model ->
                    Page.Type.Entry(
                        model = model.createPage(
                            scope = scope,
                        )
                    )
                },
                ifTransfer = { model ->
                    Page.Type.Transfer(
                        model = model.createPage(
                            scope = scope,
                        )
                    )
                },
            )
        }
    )

    internal val type: StateFlow<Editable<TransactionInfo.Type>> = typeModel.flatMapState(scope) { typeModel ->
        typeModel.fold(
            ifEntry = { model -> model.entry },
            ifTransfer = { model -> model.transfer },
        )
    }

    val goBackHandler: GoBackHandler =
        typeModel.flatMapState(scope, Type::goBackHandler)
}