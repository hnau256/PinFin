package hnau.pinfin.data.storage.impl

import arrow.core.identity
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.dto.BudgetId
import hnau.pinfin.data.storage.BudgetStorage
import hnau.pinfin.data.storage.BudgetsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class FileBasedBudgetsStorage private constructor(
    private val scope: CoroutineScope,
    private val budgetsDir: File,
    initialBudgetsNames: List<String>,
) : BudgetsStorage {

    private val namesList: MutableStateFlow<List<String>> =
        initialBudgetsNames.toMutableStateFlowAsInitial()

    override val list: StateFlow<List<BudgetStorage>> = namesList.mapListReusable(
        scope = scope,
        extractKey = ::identity,
    ) { budgetScope, name ->
        val budgetFile = File(budgetsDir, name)
        FileBasedBudgetStorage(
            scope = budgetScope,
            id = BudgetId.stringMapper.direct(name),
            budgetFile = budgetFile,
        )
    }

    override fun createNewBudget() {
        namesList.update { existingBudgetsIds ->
            existingBudgetsIds + BudgetId.new().let(BudgetId.stringMapper.reverse)
        }
    }

    class Factory(
        private val budgetsDir: File,
    ) : BudgetsStorage.Factory {

        override suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage {
            val names = withContext(Dispatchers.IO) {
                budgetsDir.list().orEmpty().toList()
            }
            return FileBasedBudgetsStorage(
                scope = scope,
                budgetsDir = budgetsDir,
                initialBudgetsNames = names,
            )
        }
    }
}