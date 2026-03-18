package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.BudgetStateBuilder
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
import org.hnau.pinfin.model.utils.budget.storage.UpchainStorage
import org.hnau.pinfin.model.utils.budget.storage.addUpdate

class BudgetRepository(
    scope: CoroutineScope,
    val state: StateFlow<BudgetState>,
    val upchainStorage: UpchainStorage,
    val remove: suspend () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun budgretStateBuilder(): BudgetStateBuilder.Dependencies
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
        scope = scope,
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
            dependencies: Dependencies,
            remove: suspend () -> Unit,
        ): BudgetRepository {
            val upchainFlow = upchainStorage.upchain
            val initialState = BudgetStateBuilder
                .empty(
                    dependencies = dependencies.budgretStateBuilder(),
                )
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
                scope = scope,
                state = state,
                upchainStorage = upchainStorage,
                remove = remove,
            )
        }
    }
}