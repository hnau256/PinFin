package org.hnau.pinfin.projector.budget.analytics.graph.configured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.Separator
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Overcompose
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.rememberLet
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.configured.GraphConfigModel
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.budget.analytics.graph.utils.format

class GraphConfigProjector(
    scope: CoroutineScope,
    private val model: GraphConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun pages(): GraphPagesProjector.Dependencies
    }

    val key: String
        get() = model.key

    private val pages: StateFlow<Loadable<GraphPagesProjector>> =
        model.pages.mapWithScope(scope) { scope, pagesOrLoading ->
            pagesOrLoading.map { (_, pages) -> //TODO handle delayed
                GraphPagesProjector(
                    scope = scope,
                    model = pages,
                    dependencies = dependencies.pages(),
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Overcompose(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            top = { contentPadding ->
                ConfigCard(
                    contentPadding = contentPadding,
                )
            },
        ) { contentPadding ->
            pages
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { pages ->
                    pages.Content(
                        contentPadding = contentPadding,
                    )
                }
        }
    }

    @Composable
    private fun ConfigCard(
        contentPadding: PaddingValues,
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .padding(
                    horizontal = Dimens.horizontalDisplayPadding,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                Separator()
                Config(
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = model.configure,
                ) {
                    Icon(
                        Icons.Default.Settings,
                    )
                }
            }
        }
    }

    @Composable
    private fun Config(
        modifier: Modifier,
    ) {
        Text(
            modifier = modifier,
            text = model
                .config
                .rememberLet { config ->

                    val period = config.split.period.format(
                        localization = dependencies.localization,
                    )

                    when (val operation = config.page.operation) {
                        AnalyticsPageConfig.Operation.Sum -> dependencies
                            .localization
                            .sumFor(period)

                        is AnalyticsPageConfig.Operation.Average -> dependencies
                            .localization
                            .avgFor(
                                period,
                                operation.subperiod.format(
                                    localization = dependencies.localization,
                                )
                            )
                    }
                }
        )
    }
}