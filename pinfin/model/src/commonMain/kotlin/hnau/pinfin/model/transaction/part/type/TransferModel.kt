package hnau.pinfin.model.transaction.part.type

import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.page.type.TypePageModel
import hnau.pinfin.model.transaction.page.type.TransferPageModel
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class TransferModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) : TypePartModel {

    @Pipe
    interface Dependencies {

        fun page(): TransferPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var part: TransferPageModel.Skeleton? = null,
    ) : TypePartModel.Skeleton {

        companion object {

            fun createForNew(): Skeleton = Skeleton()

            fun createForEdit(
                type: TransactionInfo.Type.Transfer,
            ): Skeleton = Skeleton(

            )
        }
    }

    override fun createPage(
        scope: CoroutineScope,
            ): TypePageModel = TransferPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::part
            .toAccessor()
            .getOrInit { TransferPageModel.Skeleton() },
            )
}