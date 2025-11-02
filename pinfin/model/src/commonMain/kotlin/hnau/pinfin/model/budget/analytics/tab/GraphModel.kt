@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab

import arrow.core.NonEmptyList
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.valueOrElse
import hnau.pinfin.model.utils.analytics.GraphConfig
import hnau.pinfin.model.utils.analytics.GraphProvider
import hnau.pinfin.model.utils.analytics.GraphProviderImpl
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Clock

class GraphModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val selectedPeriodIndex: MutableStateFlow<Pair<UpchainHash?, Int>?> = null.toMutableStateFlowAsInitial()
    )

    private val graphProvider: GraphProvider = GraphProviderImpl(
        config = config,
    )

    private data class State(
        val items: NonEmptyList<GraphProvider.Item>,
        val index: Int,
        val hash: UpchainHash?,
    )

    private val itemsWithIndex: StateFlow<Loadable<State?>> =
        dependencies
            .budgetRepository
            .state
            .flatMapWithScope(scope) { scope, state ->
                LoadableStateFlow(scope) {
                    graphProvider.getItems(state)?.let { items ->
                        items to state.hash
                    }
                }
            }
            .combineStateWith(
                scope = scope,
                other = skeleton.selectedPeriodIndex,
            ) { itemsOrEmptyOrLoading, indexOrNone ->
                itemsOrEmptyOrLoading.map { itemsOrEmpty ->
                    itemsOrEmpty?.let { (items, hash) ->
                        val index = indexOrNone
                            ?.takeIf { it.first == hash }
                            ?.second
                            .ifNull { items.lastIndex }
                        State(
                            items = items,
                            index = index,
                            hash = hash,
                        )
                    }
                }
            }

    val item: StateFlow<Loadable<GraphProvider.Item?>> =
        itemsWithIndex.mapState(scope) { itemsOrEmptyOrLoading ->
            itemsOrEmptyOrLoading.map { itemsOrEmpty ->
                itemsOrEmpty?.let { (items, index) ->
                    items[index]
                }
            }
        }

    private fun createMoveAction(
        scope: CoroutineScope,
        skeleton: Skeleton,
        offset: Int,
    ): StateFlow<(() -> Unit)?> = itemsWithIndex.mapState(scope) { itemsOrEmptyOrLoading ->
        itemsOrEmptyOrLoading
            .valueOrElse { null }
            ?.let { (items, index, hash) ->
                (index + offset)
                    .takeIf { it >= 0 }
                    ?.takeIf { it <= items.lastIndex }
                    ?.let { newIndex -> newIndex to hash }

            }
            ?.let { (newIndex, hash) ->
                { skeleton.selectedPeriodIndex.value = hash to newIndex }
            }
    }

    val switchToPrevious: StateFlow<(() -> Unit)?> = createMoveAction(
        scope = scope,
        skeleton = skeleton,
        offset = -1,
    )

    val switchToNext: StateFlow<(() -> Unit)?> = createMoveAction(
        scope = scope,
        skeleton = skeleton,
        offset = 1,
    )

    companion object {

        private val config: GraphConfig = GraphConfig(
            period = GraphConfig.Period.Fixed(
                duration = DatePeriod(months = 1),
                startOfOneOfPeriods = Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .let { today ->
                        LocalDate(
                            year = today.year,
                            month = today.month,
                            day = 1,
                        )
                    }
            ),
            subPeriod = DatePeriod(
                days = 1,
            ),
            view = GraphConfig.View.Column,
            operation = GraphConfig.Operation.Sum,
            groupBy = GraphConfig.GroupBy.Category,
            usedAccounts = null,
            usedCategories = null,
        )
    }

}