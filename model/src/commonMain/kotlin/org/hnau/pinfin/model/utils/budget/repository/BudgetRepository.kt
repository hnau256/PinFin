package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype
import org.hnau.pinfin.model.utils.budget.state.prototype.toBudgetState
import org.hnau.pinfin.model.utils.budget.state.prototype.withNewUpchain
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
import org.hnau.upchain.core.repository.upchain.UpchainRepository
import org.hnau.upchain.core.repository.upchain.addUpdate

class BudgetRepository(
    scope: CoroutineScope,
    val state: StateFlow<BudgetState>,
    val upchainRepository: UpchainRepository,
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
        upchainRepository.addUpdate(
            UpdateType.updateTypeMapper.reverse(update)
        )
    }

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            id: BudgetId,
            upchainRepository: UpchainRepository,
            remove: suspend () -> Unit,
        ): BudgetRepository {
            val upchainFlow = upchainRepository.upchain
            val initialState = BudgetStatePrototype
                .empty
                .withNewUpchain(upchainFlow.value)
            val state = upchainFlow
                .runningFold(
                    initial = initialState,
                ) { acc, upchain ->
                    acc.withNewUpchain(upchain)
                }
                .map { budgetStatePrototype ->
                    budgetStatePrototype.toBudgetState(
                        id = id,
                    )
                }
                .stateIn(scope)
            return BudgetRepository(
                scope = scope,
                state = state,
                upchainRepository = upchainRepository,
                remove = remove,
            )
        }
    }
}