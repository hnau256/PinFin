@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetslist

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.app.preferences.Preferences
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetId
import hnau.pinfin.repository.BudgetRepository
import hnau.pinfin.repository.storage.BudgetsStorage
import hnau.pinfin.model.budgetslist.item.BudgetItemModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsListModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        val deferredBudgetRepositories: StateFlow<Map<BudgetId, Deferred<BudgetRepository>>>

        fun item(
            id: BudgetId,
            deferredRepository: Deferred<BudgetRepository>,
        ): BudgetItemModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var itemSkeletons: Map<BudgetId, BudgetItemModel.Skeleton> = emptyMap(),
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
        deferredBudgetRepositories: Map<BudgetId, Deferred<BudgetRepository>>,
        previousItems: List<ItemInfoWithScope>,
    ): List<ItemInfoWithScope> {
        val itemsCache = previousItems
            .associateBy { it.info.id }
            .toMutableMap()
        val result = deferredBudgetRepositories.map { (id, deferredRepository) ->
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
                            deferredRepository = deferredRepository,
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

    val items: StateFlow<List<ItemInfo>> = dependencies
        .deferredBudgetRepositories
        .runningFoldState(
            scope = scope,
            createInitial = { deferredBudgetRepositories ->
                updateItems(
                    deferredBudgetRepositories = deferredBudgetRepositories,
                    previousItems = emptyList(),
                )
            },
            operation = { previousItems, deferredBudgetRepositories ->
                updateItems(
                    deferredBudgetRepositories = deferredBudgetRepositories,
                    previousItems = previousItems,
                )
            }
        )
        .mapState(
            scope = scope,
        ) { infos ->
            infos.map(ItemInfoWithScope::info)
        }

    fun createNewBudget() {
        dependencies.budgetsStorage.createNewBudget()
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}