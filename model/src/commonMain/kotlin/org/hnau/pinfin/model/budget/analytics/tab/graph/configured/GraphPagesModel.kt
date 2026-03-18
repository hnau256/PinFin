@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph.configured

import arrow.core.NonEmptyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.AnalyticsPage
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig

class GraphPagesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    private val pages: NonEmptyList<AnalyticsPage>,
    config: AnalyticsPageConfig,
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

    val pageWithIndex: StateFlow<IndexedValue<GraphPageModel>> = skeleton
        .index
        .mapWithScope(scope) { scope, indexOrNull ->
            val lastIndex = pages.lastIndex
            val index = indexOrNull
                ?.takeIf { index -> index in 0..lastIndex }
                .ifNull { lastIndex }
            val page = pages[index]
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
                config = config,
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

    val goBackHandler: GoBackHandler = skeleton
        .index
        .mapState(scope) { indexOrNull ->
            indexOrNull?.let {
                { skeleton.index.value = null }
            }
        }
}