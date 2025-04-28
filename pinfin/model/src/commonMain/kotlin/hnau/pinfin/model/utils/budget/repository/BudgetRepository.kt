package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.BudgetStateBuilder
import hnau.pinfin.model.utils.budget.state.updateTypeMapper
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.storage.addUpdate
import hnau.pinfin.model.utils.budget.upchain.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class BudgetRepository(
    scope: CoroutineScope,
    private val upchainStorage: UpchainStorage,
) {

    val state: StateFlow<BudgetState> = upchainStorage.upchain
        .runningFoldState(
            scope = scope,
            createInitial = { upchain ->
                BudgetStateBuilder
                    .empty
                    .withNewUpchain(upchain)
            },
        ) { stateBuilder, upchain ->
            stateBuilder.withNewUpchain(upchain)
        }
        .mapState(scope) { stateBuilder ->
            stateBuilder.toBudgetState()
        }

    val transactions: BudgetRepositoryTransactionsDelegate = BudgetRepositoryTransactionsDelegate(
        state = state,
        addUpdate = ::applyUpdate,
    )

    val categories: BudgetRepositoryCategoriesDelegate = BudgetRepositoryCategoriesDelegate(
        state = state,
        addUpdate = ::applyUpdate,
    )

    val accounts: BudgetRepositoryAccountsDelegate = BudgetRepositoryAccountsDelegate(
        state = state,
        addUpdate = ::applyUpdate,
    )

    private suspend fun applyUpdate(
        update: UpdateType,
    ) {
        upchainStorage.addUpdate(
            UpdateType.updateTypeMapper.reverse(update)
        )
    }
}