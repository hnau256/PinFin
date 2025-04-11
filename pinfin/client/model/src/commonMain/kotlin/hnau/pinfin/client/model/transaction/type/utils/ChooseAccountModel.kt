@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.model.utils.choose.ChooseState
import hnau.pinfin.scheme.AccountId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ChooseAccountModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    localUsedAccounts: StateFlow<Set<AccountId>>,
    dependencies: Dependencies,
    selected: StateFlow<AccountId?>,
    updateSelected: (AccountId) -> Unit,
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

    private val accounts: StateFlow<Loadable<List<AccountId>>> = dependencies
        .repository
        .account
        .list
        .combineStateWith(
            scope = scope,
            other = localUsedAccounts,
        ) { accountsOrLoading, localUsedAccounts ->
            accountsOrLoading.map { accounts ->
                accounts
                    .toSet()
                    .plus(localUsedAccounts)
                    .sorted()
            }
        }

    val state: ChooseState<AccountId> = ChooseState(
        scope = scope,
        variants = accounts,
        selected = selected.mapState(scope) { it.toOption() },
        updateSelected = updateSelected,
        query = skeleton.query,
        extractId = { id },
        extractAdditionalFields = {
            //TODO title
            emptyList()
        },
        createPossibleNewVariantsByQuery = { query ->
            listOf(AccountId(query))
        },
        onReady = onReady,
    )
}