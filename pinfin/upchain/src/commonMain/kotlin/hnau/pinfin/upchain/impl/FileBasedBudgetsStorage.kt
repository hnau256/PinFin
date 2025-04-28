package hnau.pinfin.upchain.impl

import arrow.core.identity
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.BudgetId
import hnau.pinfin.upchain.BudgetUpchain
import hnau.pinfin.upchain.BudgetsStorage
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

    override val list: StateFlow<List<Pair<BudgetId, BudgetUpchain>>> = namesList.mapListReusable(
        scope = scope,
        extractKey = ::identity,
    ) { budgetScope, name ->
        val budgetFile = File(budgetsDir, name)
        val id = BudgetId.Companion.stringMapper.direct(name)
        val upchain = FileBasedBudgetUpchain(
            scope = budgetScope,
            budgetFile = budgetFile,
        )
        id to upchain
    }

    override fun createNewBudget() {
        namesList.update { existingBudgetsIds ->
            existingBudgetsIds + BudgetId.Companion.new().let(BudgetId.Companion.stringMapper.reverse)
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