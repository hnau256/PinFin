package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype
import org.hnau.pinfin.model.utils.budget.state.prototype.toBudgetState
import org.hnau.pinfin.model.utils.budget.state.prototype.withNewUpchain
import org.hnau.pinfin.model.utils.budget.state.toConfig
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
import org.hnau.upchain.core.repository.upchain.UpchainRepository
import org.hnau.upchain.core.repository.upchain.addUpdates

class BudgetRepository(
    val state: StateFlow<BudgetState>,
    @Deprecated("Use BudgetRepository.applyUpdate instead")
    val upchainRepository: UpchainRepository,
    val remove: suspend () -> Unit,
) {

    private val _upchainEditVersion: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    val upchainEditVersion: StateFlow<Int>
        get() = _upchainEditVersion

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
        val delta = newInfo - info
        if (delta == BudgetConfig.empty) {
            return
        }
        applyUpdate(
            update = UpdateType.Config(
                config = delta,
            )
        )
    }

    suspend fun applyUpdates(
        updates: List<UpdateType>,
    ) {
        @Suppress("DEPRECATION")
        upchainRepository.addUpdates(
            updates = updates.map(
                transform = UpdateType.updateTypeMapper.reverse,
            )
        )
        _upchainEditVersion.update { it + 1 }
    }

    suspend fun applyUpdate(
        update: UpdateType,
    ) {
        applyUpdates(
            listOf(update)
        )
    }

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            id: BudgetId,
            initialConfig: BudgetConfig,
            upchainRepository: UpchainRepository,
            remove: suspend () -> Unit,
        ): BudgetRepository {
            val upchainFlow = upchainRepository.upchain
            val initialState = BudgetStatePrototype
                .empty
                .copy(
                    config = initialConfig,
                )
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
                state = state,
                upchainRepository = upchainRepository,
                remove = remove,
            )
        }
    }
}