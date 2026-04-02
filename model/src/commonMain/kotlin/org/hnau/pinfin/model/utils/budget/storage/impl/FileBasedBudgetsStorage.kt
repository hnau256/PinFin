package org.hnau.pinfin.model.utils.budget.storage.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.mapListReusable
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.upchainUIdMapper
import org.hnau.upchain.core.repository.file.upchains.fileBased
import org.hnau.upchain.core.repository.upchains.UpchainsRepository

fun BudgetsStorage.Factory.Companion.files(
    dependencies: BudgetsStorage.Factory.Dependencies,
    budgetsDir: String,
): BudgetsStorage.Factory = BudgetsStorage.Factory { scope ->

    val upchains = UpchainsRepository.fileBased(
        dir = budgetsDir,
    )

    val deferredStorages = upchains
        .upchains
        .mapListReusable(
            scope = scope,
            extractKey = UpchainsRepository.Item::id,
            transform = { scope, item ->
                val id = item.id.let(BudgetId.upchainUIdMapper.direct)
                scope.async {
                    val repository = BudgetRepository.create(
                        scope = scope,
                        id = id,
                        upchainRepository = item.repository,
                        dependencies = dependencies.budgetRepository(),
                        remove = item.remove,
                    )
                    KeyValue(id, repository)
                }
            }
        )
        .map { listOfDeferred -> listOfDeferred.awaitAll() }
        .stateIn(scope)

    object : BudgetsStorage {

        override val list: StateFlow<List<KeyValue<BudgetId, BudgetRepository>>>
            get() = deferredStorages

        override suspend fun createNewBudgetIfNotExists(
            id: BudgetId,
        ) {
            upchains.createUpchain(
                id = id.let(BudgetId.upchainUIdMapper.reverse)
            )
        }
    }
}