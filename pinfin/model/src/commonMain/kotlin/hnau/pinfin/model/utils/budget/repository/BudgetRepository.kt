package hnau.pinfin.model.utils.budget.repository

import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.data.BudgetId
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.BudgetStateBuilder
import hnau.pinfin.model.utils.budget.state.updateTypeMapper
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.storage.addUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

class BudgetRepository(
    val state: StateFlow<BudgetState>,
    val upchainStorage: UpchainStorage,
    val remove: suspend () -> Unit,
) {

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

    suspend fun config(
        config: BudgetConfig,
    ) {
        val info = state.value.info
        val newInfo = info + config
        if (info == newInfo) {
            return
        }
        applyUpdate(
            update = UpdateType.Config(
                config = config,
            )
        )
    }

    private suspend fun applyUpdate(
        update: UpdateType,
    ) {
        upchainStorage.addUpdate(
            UpdateType.updateTypeMapper.reverse(update)
        )
    }

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            id: BudgetId,
            upchainStorage: UpchainStorage,
            remove: suspend () -> Unit,
        ): BudgetRepository {
            val upchainFlow = upchainStorage.upchain
            val initialState = BudgetStateBuilder
                .empty
                .withNewUpchain(upchainFlow.value)
            val state = upchainFlow
                .runningFold(
                    initial = initialState,
                ) { acc, upchain ->
                    acc.withNewUpchain(upchain)
                }
                .map { budgetStateBuilder ->
                    budgetStateBuilder.toBudgetState(
                        id = id,
                    )
                }
                .stateIn(scope)
            return BudgetRepository(
                state = state,
                upchainStorage = upchainStorage,
                remove = remove,
            )
        }
    }
}