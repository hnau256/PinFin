package hnau.pinfin.model.transaction_old_2.part.type

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction_old_2.page.type.TransferPageModel
import hnau.pinfin.model.transaction_old_2.page.type.TypePageModel
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
) {

    @Pipe
    interface Dependencies {

        fun page(): TransferPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var part: TransferPageModel.Skeleton? = null,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton()

            fun createForEdit(
                type: TransactionInfo.Type.Transfer,
            ): Skeleton = Skeleton(

            )
        }
    }

    fun createPage(
        scope: CoroutineScope,
            ): TypePageModel = TransferPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::part
            .toAccessor()
            .getOrInit { TransferPageModel.Skeleton() },
            )

    val goBackHandler: GoBackHandler
        get() = TODO()
}