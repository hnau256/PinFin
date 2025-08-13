package hnau.pinfin.model.transaction.part.type

import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.page.type.PageTypeModel
import hnau.pinfin.model.transaction.page.type.TransferPageModel
import hnau.pinfin.model.transaction.utils.NavAction
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
) : PartTypeModel {

    @Pipe
    interface Dependencies {

        fun page(): TransferPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var part: TransferPageModel.Skeleton? = null,
    ) : PartTypeModel.Skeleton {

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
        navAction: NavAction
    ): PageTypeModel = TransferPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::part
            .toAccessor()
            .getOrInit { TransferPageModel.Skeleton() },
        navAction = navAction,
    )
}