package hnau.pinfin.model.utils.budget.storage.impl

import hnau.common.app.model.file.File
import hnau.common.app.model.file.delete
import hnau.common.app.model.file.exists
import hnau.common.app.model.file.list
import hnau.common.app.model.file.plus
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.upchain.Sha256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

fun BudgetsStorage.Factory.Companion.files(
    sha256: Sha256,
    budgetsDir: File,
): BudgetsStorage.Factory = BudgetsStorage.Factory { scope ->

    val accessStoragesMutex = Mutex()

    var storages: MutableStateFlow<List<Pair<BudgetId, BudgetRepository>>>? = null

    val createBudgetRepository: suspend (
        id: BudgetId,
    ) -> BudgetRepository = { id ->
        val file = budgetsDir + id.let(BudgetId.stringMapper.reverse)
        val upchainStorage = FileBasedUpchainStorage.create(
            scope = scope,
            budgetFile = file,
            sha256 = sha256,
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
            },
            sha256 = sha256,
        )
    }

    storages = withContext(Dispatchers.IO) {
        budgetsDir
            .takeIf(File::exists)
            ?.list()
            .ifNull { emptyList() }
            .map { budgetFile ->
                val id: BudgetId = BudgetId.stringMapper.direct(budgetFile.path.name)
                val budgetRepository = scope.async { createBudgetRepository(id) }
                id to budgetRepository
            }
            .map { (id, budgetRepository) ->
                val budgetRepository = budgetRepository.await()
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