package hnau.pinfin.model.utils.budget.storage.impl

import arrow.core.identity
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class FileBasedBudgetsStorage private constructor(
    private val scope: CoroutineScope,
    private val budgetsDir: File,
    initialBudgetsNames: Set<String>,
) : BudgetsStorage {

    private val namesList: MutableStateFlow<Set<String>> =
        initialBudgetsNames.toMutableStateFlowAsInitial()

    override val list: StateFlow<List<Pair<BudgetId, Deferred<UpchainStorage>>>> =
        namesList
            .mapState(
                scope = scope,
                transform = Iterable<String>::toList,
            )
            .mapListReusable(
                scope = scope,
                extractKey = ::identity,
            ) { budgetScope, name ->

                val id = BudgetId.Companion.stringMapper.direct(name)
                val upchainStorage = budgetScope.async {
                    FileBasedUpchainStorage.create(
                        scope = budgetScope,
                        budgetFile = File(budgetsDir, name),
                    )
                }

                id to upchainStorage
            }

    override suspend fun createNewBudgetIfNotExists(
        id: BudgetId,
    ) {
        namesList.update { existingBudgetsIds ->
            existingBudgetsIds + id.let(BudgetId.Companion.stringMapper.reverse)
        }
    }

    class Factory(
        private val budgetsDir: File,
    ) : BudgetsStorage.Factory {

        override suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage {
            val names = withContext(Dispatchers.IO) {
                budgetsDir
                    .list()
                    .orEmpty()
                    .toSet()
            }
            return FileBasedBudgetsStorage(
                scope = scope,
                budgetsDir = budgetsDir,
                initialBudgetsNames = names,
            )
        }
    }
}

fun BudgetsStorage.Factory.Companion.files(
    budgetsDir: File,
): BudgetsStorage.Factory = FileBasedBudgetsStorage.Factory(
    budgetsDir = budgetsDir,
)