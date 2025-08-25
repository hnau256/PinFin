@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AccountModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun chooseOrCreate(): ChooseOrCreateModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var chooseOrCreate: ChooseOrCreateModel.Skeleton? = null,
        val account: MutableStateFlow<AccountInfo?>,
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
    ): ChooseOrCreateModel<AccountInfo> = ChooseOrCreateModel(
        scope = scope,
        dependencies = dependencies.chooseOrCreate(),
        skeleton = skeleton::chooseOrCreate
            .toAccessor()
            .getOrInit { ChooseOrCreateModel.Skeleton() },
        extractItemsFromState = BudgetState::visibleAccounts,
        additionalItems = skeleton.account.mapState(scope, ::listOfNotNull),
        itemTextMapper = Mapper(
            direct = AccountInfo::title,
            reverse = { title ->
                AccountInfo(
                    id = AccountId(title),
                    config = null,
                    amount = Amount.zero,
                )
            }
        ),
        selected = skeleton.account.mapState(scope, AccountInfo?::toOption),
        onReady = { selected ->
            skeleton.account.value = selected
            //TODO go forward
        }
    )

    val account: StateFlow<AccountInfo?>
        get() = skeleton.account

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}