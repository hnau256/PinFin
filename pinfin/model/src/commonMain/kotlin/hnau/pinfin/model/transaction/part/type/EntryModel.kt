package hnau.pinfin.model.transaction.part.type

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.page.type.EntryPageModel
import hnau.pinfin.model.transaction.page.type.PageTypeModel
import hnau.pinfin.model.transaction.part.type.entry.EntryPart
import hnau.pinfin.model.transaction.part.type.entry.AccountPartModel
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class EntryModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val requestFocus: () -> Unit,
    private val isFocused: StateFlow<Boolean>,
) : PartTypeModel {

    @Pipe
    interface Dependencies {

        fun page(): EntryPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: EntryPageModel.Skeleton? = null,
        val part: MutableStateFlow<EntryPart> =
            EntryPart.default.toMutableStateFlowAsInitial(),
        val account: AccountPartModel.Skeleton,
    ) : PartTypeModel.Skeleton {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                account = AccountPartModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                type: TransactionInfo.Type.Entry,
            ): Skeleton = Skeleton(
                account = AccountPartModel.Skeleton.createForEdit(
                    account = type.account,
                ),
            )
        }
    }

    override fun createPage(
        scope: CoroutineScope,
        navAction: NavAction
    ): PageTypeModel = EntryPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { EntryPageModel.Skeleton() },
        navAction = navAction,
    )
}