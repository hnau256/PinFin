package org.hnau.pinfin.model.utils.budget.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.storage.impl.FileBasedUpchainStorage

interface BudgetsStorage {

    val list: StateFlow<List<Pair<BudgetId, BudgetRepository>>>

    suspend fun createNewBudgetIfNotExists(
        id: BudgetId,
    )

    fun interface Factory {

        @Pipe
        interface Dependencies {

            fun budgetRepository(): BudgetRepository.Dependencies

            fun fileBasedUpchainStorage(): FileBasedUpchainStorage.Dependencies

            companion object
        }

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage

        companion object
    }
}