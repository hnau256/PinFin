package hnau.pinfin.repository

import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.repository.dto.UpdateType
import hnau.pinfin.upchain.BudgetUpchain
import hnau.pinfin.upchain.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class BudgetRepository(
    scope: CoroutineScope,
    initialState: BudgetStateBuilder,
    private val addUpdate: suspend (UpdateType) -> Unit,
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
        update: UpdateType,
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
            budgetUpchain: BudgetUpchain,
        ): BudgetRepository {
            val initialState = BudgetStateBuilder()
            budgetUpchain.useUpdates { updates ->
                updates.forEach { update ->
                    val updateType = json.decodeFromString(
                        UpdateType.serializer(),
                        update.value,
                    )
                    initialState.applyUpdate(updateType)
                }
            }
            return BudgetRepository(
                scope = scope,
                initialState = initialState,
                addUpdate = { updateType ->
                    val update = json
                        .encodeToString(
                            UpdateType.serializer(),
                            updateType,
                        )
                        .let(::Update)
                    budgetUpchain.addUpdates(listOf(update))
                },
            )
        }


        private val json: Json = Json.Default
    }
}