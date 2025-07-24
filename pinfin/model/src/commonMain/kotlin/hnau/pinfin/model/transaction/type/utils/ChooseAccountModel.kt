@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.choose.ChooseState
import hnau.pipe.annotations.Pipe
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

    @Pipe
    interface Dependencies {

        val repository: BudgetRepository
    }

    private val accounts: StateFlow<List<AccountInfo>> = dependencies
        .repository
        .accounts
        .getList(
            includeInvisible = false,
        )
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
                    amount = Amount.zero,
                    config = null,
                )
            )
        },
        onReady = onReady,
    )

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}