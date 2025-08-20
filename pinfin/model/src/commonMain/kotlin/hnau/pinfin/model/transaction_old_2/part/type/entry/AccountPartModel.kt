package hnau.pinfin.model.transaction_old_2.part.type.entry

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction_old_2.page.type.entry.AccountPageModel
import hnau.pinfin.model.transaction_old_2.page.type.entry.EntryPagePageModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class AccountPartModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) {

    @Pipe
    interface Dependencies {

        fun page(): AccountPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val account: MutableStateFlow<AccountInfo?>,
        var page: AccountPageModel.Skeleton? = null,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                account = null.toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                account: AccountInfo,
            ): Skeleton = Skeleton(
                account = account.toMutableStateFlowAsInitial(),
            )
        }
    }

    fun createPage(
        scope: CoroutineScope,
            ): EntryPagePageModel = AccountPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { AccountPageModel.Skeleton() },
                account = skeleton.account,
    )
}