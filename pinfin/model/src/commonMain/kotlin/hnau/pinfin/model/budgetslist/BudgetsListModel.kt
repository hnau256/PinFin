@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetslist

import arrow.core.NonEmptySet
import arrow.core.toNonEmptySetOrNull
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
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
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsListModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val syncOpener: SyncOpener

        val budgetsStorage: BudgetsStorage

        val budgetRepositories: StateFlow<Map<BudgetId, BudgetRepository>>

        fun item(
            id: BudgetId,
            repository: BudgetRepository,
        ): BudgetItemModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var itemSkeletons: Map<BudgetId, BudgetItemModel.Skeleton> = emptyMap(),
    )

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
        val skeleton: BudgetItemModel.Skeleton,
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
                    val skeleton = skeleton
                        .itemSkeletons[id]
                        ?: BudgetItemModel.Skeleton()

                    val itemScope = scope.createChild()
                    val model = BudgetItemModel(
                        scope = itemScope,
                        dependencies = dependencies.item(
                            id = id,
                            repository = repository,
                        ),
                        skeleton = skeleton,
                    )
                    ItemInfoWithScope(
                        info = ItemInfo(
                            id = id,
                            model = model,
                        ),
                        scope = scope,
                        skeleton = skeleton,
                    )
                }
        }
        itemsCache.values.forEach { it.scope.cancel() }
        skeleton.itemSkeletons = result.associate { it.info.id to it.skeleton }
        return result
    }

    val items: StateFlow<NonEmptySet<ItemInfo>?> = dependencies
        .budgetRepositories
        .mapState(scope) { repositories -> repositories.toList() }
        .mapListReusable(
            scope = scope,
            extractKey = Pair<BudgetId, *>::first,
            transform = { budgetScope, (id, repository) ->
                val skeleton = skeleton
                    .itemSkeletons[id]
                    ?: BudgetItemModel.Skeleton()

                val model = BudgetItemModel(
                    scope = budgetScope,
                    dependencies = dependencies.item(
                        id = id,
                        repository = repository,
                    ),
                    skeleton = skeleton,
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

    private val inProgressRegistry = InProgressRegistry()

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

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}