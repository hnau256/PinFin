package hnau.pinfin.model.budget.analytics.tab.graph

import arrow.core.NonEmptyList
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.pinfin.model.utils.analytics.AnalyticsEntry
import hnau.pinfin.model.utils.analytics.AnalyticsPage
import hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class GraphPagesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    private val pages: NonEmptyList<AnalyticsPage>,
    pageConfig: AnalyticsPageConfig,
) {

    @Pipe
    interface Dependencies {

        val analyticsEntries: List<AnalyticsEntry>

        fun page(
            analyticsEntries: List<AnalyticsEntry>,
        ): GraphPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val index: MutableStateFlow<Int?> = null.toMutableStateFlowAsInitial(),
        var page: Pair<Int, GraphPageModel.Skeleton>? = null,
    )

    private val pageWithIndex: StateFlow<IndexedValue<GraphPageModel>> = skeleton
        .index
        .mapWithScope(scope) { scope, indexOrNull ->
            val lastIndex = pages.lastIndex
            val index = indexOrNull
                ?.takeIf { index -> index in 0..lastIndex }
                .ifNull { lastIndex }
            val page = pages[lastIndex]
            val pageSkeleton: GraphPageModel.Skeleton = skeleton
                .page
                ?.takeIf { it.first == index }
                ?.second
                .ifNull {
                    GraphPageModel.Skeleton().also { newPageSkeleton ->
                        skeleton.page = index to newPageSkeleton
                    }
                }
            val model = GraphPageModel(
                scope = scope,
                dependencies = dependencies.page(
                    analyticsEntries = dependencies
                        .analyticsEntries
                        .filter { entry ->
                            entry.date in page.period
                        },
                ),
                skeleton = pageSkeleton,
                page = page,
                config = pageConfig,
            )
            IndexedValue(index, model)
        }

    private fun createMoveAction(
        scope: CoroutineScope,
        skeleton: Skeleton,
        offset: Int,
    ): StateFlow<(() -> Unit)?> = pageWithIndex.mapState(scope) { indexedModel ->
        indexedModel
            .let { (index) -> (index + offset).takeIf { it in 0..pages.lastIndex } }
            ?.let { index -> { skeleton.index.value = index } }
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
}