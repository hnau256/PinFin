package hnau.pinfin.sync.server

import arrow.core.identity
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.UpchainHash
import hnau.pinfin.sync.server.BudgetSyncServer.GetUpdatesResult
import hnau.pinfin.upchain.BudgetId
import hnau.pinfin.upchain.BudgetsStorage
import hnau.pinfin.upchain.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetsSyncServer(
    scope: CoroutineScope,
    budgetsStorage: BudgetsStorage,
) {

    private val budgetsUpdates: StateFlow<Map<BudgetId, BudgetSyncServer>> = budgetsStorage
        .list
        .mapListReusable(
            scope = scope,
            extractKey = { (id) -> id },
            transform = { budgetScope, (id, upchain) ->
                val budgetSyncServer = BudgetSyncServer(
                    scope = budgetScope,
                    upchain = upchain,
                )
                id to budgetSyncServer
            }
        )
        .mapState(scope) { budgets ->
            budgets.associate(::identity)
        }


    suspend fun getBudgets(): ApiResponse<Map<BudgetId, UpchainHash>> = ApiResponse.Success(
        data = budgetsUpdates
            .value
            .mapValues { (_, budgetSyncServer) -> budgetSyncServer.getPeekHash() }
    )

    suspend fun checkContainsOneOfHashes(
        budgetId: BudgetId,
        hashesToCheck: List<UpchainHash>,
    ): ApiResponse<UpchainHash?> = withBudget(
        budgetId = budgetId,
    ) { budget ->
        ApiResponse.Success(budget.checkContainsOneOfHashes(hashesToCheck))
    }

    suspend fun getUpdates(
        budgetId: BudgetId,
        after: UpchainHash,
    ): ApiResponse<GetUpdatesResult> = withBudget(
        budgetId = budgetId,
    ) { budget ->
        budget.getUpdates(
            after = after,
        )
    }

    suspend fun addUpdates(
        budgetId: BudgetId,
        after: UpchainHash,
        updates: List<Update>,
    ): ApiResponse<Unit> = withBudget(
        budgetId = budgetId,
    ) { budget ->
        budget.addUpdates(
            after = after,
            updates = updates,
        )
    }

    private inline fun <R> withBudget(
        budgetId: BudgetId,
        block: (BudgetSyncServer) -> ApiResponse<R>,
    ): ApiResponse<R> {
        val budget: BudgetSyncServer? = budgetsUpdates.value[budgetId]
        return when (budget) {
            null -> ApiResponse.Error("Has no budget with id $budgetId")
            else -> block(budget)
        }
    }
}