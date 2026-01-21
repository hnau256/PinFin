@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetslist

import arrow.core.NonEmptySet
import arrow.core.toNonEmptySetOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.flow.state.mapListReusable
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.budgetslist.item.BudgetItemModel
import hnau.pinfin.model.budgetsstack.SyncOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.DemoBudget
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.addUpdates
import hnau.pinfin.model.utils.budget.storage.createNewBudgetIfNotExistsAndGet
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.UseSerializers

class BudgetsListModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val syncOpener: SyncOpener

        val budgetsStorage: BudgetsStorage

        val budgetRepositories: StateFlow<Map<BudgetId, BudgetRepository>>

        fun item(
            id: BudgetId,
            repository: BudgetRepository,
        ): BudgetItemModel.Dependencies
    }

    fun openSync() {
        dependencies.syncOpener.openSync()
    }

    data class ItemInfo(
        val id: BudgetId,
        val model: BudgetItemModel,
    )

    private data class ItemInfoWithScope(
        val info: ItemInfo,
        val scope: CoroutineScope,
    )

    private fun updateItems(
        budgetRepositories: Map<BudgetId, BudgetRepository>,
        previousItems: List<ItemInfoWithScope>,
    ): List<ItemInfoWithScope> {
        val itemsCache = previousItems
            .associateBy { it.info.id }
            .toMutableMap()
        val result = budgetRepositories.map { (id, repository) ->
            itemsCache
                .remove(id)
                .ifNull {
                    val itemScope = scope.createChild()
                    val model = BudgetItemModel(
                        scope = itemScope,
                        dependencies = dependencies.item(
                            id = id,
                            repository = repository,
                        ),
                    )
                    ItemInfoWithScope(
                        info = ItemInfo(
                            id = id,
                            model = model,
                        ),
                        scope = scope,
                    )
                }
        }
        itemsCache.values.forEach { it.scope.cancel() }
        return result
    }

    val items: StateFlow<NonEmptySet<ItemInfo>?> = dependencies
        .budgetRepositories
        .mapState(scope) { repositories -> repositories.toList() }
        .mapListReusable(
            scope = scope,
            extractKey = Pair<BudgetId, *>::first,
            transform = { budgetScope, (id, repository) ->

                val model = BudgetItemModel(
                    scope = budgetScope,
                    dependencies = dependencies.item(
                        id = id,
                        repository = repository,
                    ),
                )
                ItemInfo(
                    id = id,
                    model = model,
                )
            }
        )
        .mapState(scope) { items ->
            items.toNonEmptySetOrNull()
        }

    private val inProgressRegistry = InProgressRegistry(
        scope = scope,
    )

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    fun createNewBudget() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetsStorage.createNewBudgetIfNotExists(
                    id = BudgetId.new(),
                )
            }
        }
    }

    fun createDemoBudget() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                val updates = withContext(Dispatchers.Default) {
                    DemoBudget
                        .updates
                }
                dependencies
                    .budgetsStorage
                    .createNewBudgetIfNotExistsAndGet(DemoBudget.id)
                    .upchainStorage
                    .addUpdates(updates)
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}