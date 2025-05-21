@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.model.EditingString
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.common.model.toEditingString
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.AccountId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.SignedAmount
import hnau.pinfin.model.utils.choose.ChooseState
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ChooseAccountModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    localUsedAccounts: StateFlow<Set<AccountInfo>>,
    dependencies: Dependencies,
    selected: StateFlow<AccountInfo?>,
    updateSelected: (AccountInfo) -> Unit,
    onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val query: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),
    ) {

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    query = "".toEditingString().toMutableStateFlowAsInitial(),
                )
        }
    }

    @Shuffle
    interface Dependencies {

        val repository: BudgetRepository
    }

    private val accounts: StateFlow<List<AccountInfo>> = dependencies
        .repository
        .accounts
        .list
        .combineStateWith(
            scope = scope,
            other = localUsedAccounts,
        ) { accounts, localUsedAccounts ->
            (accounts + localUsedAccounts).distinct().sortedBy { it.title }
        }

    val state: ChooseState<AccountInfo> = ChooseState(
        scope = scope,
        variants = accounts,
        selected = selected.mapState(scope) { it.toOption() },
        updateSelected = updateSelected,
        query = skeleton.query,
        extractId = { it.id.id },
        extractAdditionalFields = { info ->
            listOf(
                info.title
            )
        },
        createPossibleNewVariantsByQuery = { query ->
            listOf(
                AccountInfo(
                    id = AccountId(query),
                    amount = SignedAmount.zero,
                )
            )
        },
        onReady = onReady,
    )

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}