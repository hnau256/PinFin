package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.bind
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.data.records.RecordEntry
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionTypeEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun entry(): TransactionEntryEditModel.Dependencies

        fun transfer(): TransactionTransferEditModel.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = TransactionEntryEditModel::class,
                identifier = "entry",
            ),
            Variant(
                type = TransactionTransferEditModel::class,
                identifier = "transfer",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "TransactionTypeEditModelType",
    )
    interface Type {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = TransactionEntryEditModel.Skeleton::class,
                identifier = "entry",
            ),
            Variant(
                type = TransactionTransferEditModel.Skeleton::class,
                identifier = "transfer",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "TransactionTypeEditModelTypeSkeleton",
        serializable = true,
    )
    interface TypeSkeleton {

        companion object
    }

    @Serializable
    data class Skeleton(
        val type: MutableStateFlow<TransactionTypeEditModelTypeSkeleton>,
    ) {

        companion object {

            fun createForNew(
                type: TransactionType,
            ): Skeleton = Skeleton(
                type = createTypeSkeletonForNew(
                    type = type,
                ).toMutableStateFlowAsInitial()
            )
        }
    }


    val type: StateFlow<TransactionTypeEditModelType> = skeleton
        .type
        .mapWithScope(scope) { scope, type ->
            type.fold(
                ifEntry = { entrySkeleton ->
                    Type.entry(
                        scope = scope,
                        skeleton = entrySkeleton,
                        dependencies = dependencies.entry(),
                    )
                },
                ifTransfer = { transferSkeleton ->
                    Type.transfer(
                        scope = scope,
                        skeleton = transferSkeleton,
                        dependencies = dependencies.transfer(),
                    )
                }
            )
        }

    private val TransactionTypeEditModelTypeSkeleton.variant: TransactionType
        get() = fold(
            ifEntry = { TransactionType.Entry },
            ifTransfer = { TransactionType.Transfer },
        )

    val typeVariant: StateFlow<TransactionType> = skeleton
        .type
        .mapState(scope) { typeSkeleton -> typeSkeleton.variant }

    fun setType(
        type: TransactionType,
    ) {
        skeleton
            .type
            .update { currentType ->
                if (currentType.variant == type) {
                    return@update currentType
                }
                createTypeSkeletonForNew(
                    type = type,
                )
            }
    }

    val typeEditable: StateFlow<Editable<Transaction.Type<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, RecordEntry<KeyValue<CategoryId, CategoryInfo>>>>> = derivedStateFlowOf(scope) {
        editable {
            type.state.fold(
                ifEntry = {
                    it.entryEditable.state.bind()
                },
                ifTransfer = {
                    it.transferEditable.state.bind()
                }
            )
        }
    }

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        type.state.goBackHandler.state
    }

    companion object {


        private fun createTypeSkeletonForNew(
            type: TransactionType,
        ): TransactionTypeEditModelTypeSkeleton = type.fold(
            ifEntry = {
                TransactionTypeEditModelTypeSkeleton.Entry(
                    TransactionEntryEditModel.Skeleton.createForNew()
                )
            },
            ifTransfer = {
                TransactionTypeEditModelTypeSkeleton.Transfer(
                    TransactionTransferEditModel.Skeleton.createForNew()
                )
            },
        )
    }
}