package org.hnau.pinfin.model.utils.budget.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

interface BudgetsStorage {

    val list: StateFlow<List<KeyValue<BudgetId, BudgetRepository>>>

    suspend fun createNewBudgetIfNotExists(
        id: BudgetId,
    )

    fun interface Factory {

        @Pipe
        interface Dependencies {

            fun budgetRepository(): BudgetRepository.Dependencies

            companion object
        }

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage

        companion object
    }
}