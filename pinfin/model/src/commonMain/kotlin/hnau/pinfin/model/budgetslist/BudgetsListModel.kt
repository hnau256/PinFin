package hnau.pinfin.model.budgetslist

import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.ifNull
import hnau.pinfin.data.BudgetsRepository
import hnau.pinfin.data.dto.BudgetId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class BudgetsListModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onBudgetClick: (BudgetId) -> Unit,
    val onAddBudgetClick: () -> Unit,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository

        fun item(): BudgetItemModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var itemSkeletons: List<BudgetItemModel.Skeleton> = emptyList(),
    )

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
        ids: List<BudgetId>,
        previousItems: List<ItemInfoWithScope>,
    ): List<ItemInfoWithScope> {
        val skeletonsCache = skeleton
            .itemSkeletons
            .associateBy(BudgetItemModel.Skeleton::id)
        val itemsCache = previousItems
            .associateBy { it.info.id }
            .toMutableMap()
        val result = ids.map { id ->
            itemsCache
                .remove(id)
                .ifNull {
                    val skeleton = skeletonsCache[id] ?: BudgetItemModel.Skeleton(
                        id = id,
                    )
                    val itemScope = scope.createChild()
                    val model = BudgetItemModel(
                        scope = itemScope,
                        dependencies = dependencies.item(),
                        skeleton = skeleton,
                        onClick = { onBudgetClick(id) }
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
        skeleton.itemSkeletons = result.map(ItemInfoWithScope::skeleton)
        return result
    }

    val items: StateFlow<List<ItemInfo>> = dependencies
        .budgetsRepository
        .budgets
        .runningFoldState(
            scope = scope,
            createInitial = { ids ->
                updateItems(
                    ids = ids,
                    previousItems = emptyList(),
                )
            },
            operation = { previousItems, ids ->
                updateItems(
                    ids = ids,
                    previousItems = previousItems,
                )
            }
        )
        .mapState(
            scope = scope,
        ) { infos ->
            infos.map(ItemInfoWithScope::info)
        }
}