package hnau.pinfin.model.utils.budget.storage.impl

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

fun BudgetsStorage.Factory.Companion.files(
    budgetsDir: File,
): BudgetsStorage.Factory = BudgetsStorage.Factory { scope ->

    val accessStoragesMutex = Mutex()

    var storages: MutableStateFlow<List<Pair<BudgetId, BudgetRepository>>>? = null

    val createBudgetRepository: suspend (
        id: BudgetId,
    ) -> BudgetRepository = { id ->
        val file = File(budgetsDir, id.let(BudgetId.stringMapper.reverse))
        val upchainStorage = FileBasedUpchainStorage.create(
            scope = scope,
            budgetFile = file,
        )
        BudgetRepository.create(
            scope = scope,
            id = id,
            upchainStorage = upchainStorage,
            remove = {
                file.delete()
                accessStoragesMutex.withLock {
                    storages!!.update { it.filter { it.first != id } }
                }
            }
        )
    }

    storages = withContext(Dispatchers.IO) {
        budgetsDir
            .list()
            .orEmpty()
            .map { budgetName ->
                val id: BudgetId = BudgetId.stringMapper.direct(budgetName)
                val deferredBudgetRepository = scope.async { createBudgetRepository(id) }
                id to deferredBudgetRepository
            }
            .map { (id, deferredBudgetRepository) ->
                val budgetRepository = deferredBudgetRepository.await()
                id to budgetRepository
            }
            .toMutableStateFlowAsInitial()
    }

    object : BudgetsStorage {

        override val list: StateFlow<List<Pair<BudgetId, BudgetRepository>>>
            get() = storages

        override suspend fun createNewBudgetIfNotExists(
            id: BudgetId,
        ) {
            accessStoragesMutex.withLock {
                if (storages.value.any { it.first == id }) {
                    return@withLock
                }
                val budgetRepository = createBudgetRepository(id)
                storages.update { currentStorages ->
                    currentStorages + (id to budgetRepository)
                }
            }
        }
    }
}