package hnau.pinfin.data.budget

import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.UpdateRepository
import hnau.pinfin.data.dto.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BudgetRepository(
    scope: CoroutineScope,
    initialState: BudgetStateBuilder,
    private val addUpdate: suspend (Update) -> Unit,
) {

    private val stateBuilder: MutableStateFlow<BudgetStateBuilder> =
        initialState.toMutableStateFlowAsInitial()

    val state: StateFlow<BudgetState> = stateBuilder.mapState(scope) { stateBuilder ->
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
        update: Update,
    ) {
        addUpdate.invoke(update)
        stateBuilder.update { stateBuilder ->
            stateBuilder.apply {
                applyUpdate(update)
            }
        }
    }

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            updateRepository: UpdateRepository,
        ): BudgetRepository {
            val initialState = BudgetStateBuilder()
            updateRepository.useUpdates { updates ->
                updates.forEach { update ->
                    initialState.applyUpdate(update)
                }
            }
            return BudgetRepository(
                scope = scope,
                initialState = initialState,
                addUpdate = updateRepository::addUpdate,
            )
        }
    }
}