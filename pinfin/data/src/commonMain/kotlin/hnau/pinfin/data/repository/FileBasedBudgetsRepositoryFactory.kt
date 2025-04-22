package hnau.pinfin.data.repository

import hnau.common.kotlin.ifNull
import hnau.pinfin.data.repository.budget.BudgetRepository
import hnau.pinfin.data.dto.BudgetId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileBasedBudgetsRepositoryFactory(
    private val budgetsDirectory: File,
) : BudgetsRepository.Factory {

    override suspend fun createBudgetsRepository(
        scope: CoroutineScope,
    ): BudgetsRepository = BudgetsRepository(
        scope = scope,
        initialBudgets = withContext(Dispatchers.IO) {
            budgetsDirectory
                .list()
                .ifNull { emptyArray() }
                .map(BudgetId.stringMapper.direct)
        },
        getBudgetInfo = { id ->
            val updates = FileUpdateRepository(
                updatesFile = File(budgetsDirectory, BudgetId.stringMapper.reverse(id)),
            )
            BudgetInfo(
                repository = BudgetRepository.create(
                    scope = scope,
                    updateRepository = updates,
                )
            )
        },
    )
}