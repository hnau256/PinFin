package org.hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.Tabs
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Overcompose
import org.hnau.commons.app.projector.utils.rememberPagerState
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.budget.analytics.AnalyticsModel
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTabValues
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.budget.analytics.graph.GraphProjector

@OptIn(ExperimentalMaterial3Api::class)
class AnalyticsProjector(
    scope: CoroutineScope,
    private val model: AnalyticsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun accounts(): AccountsProjector.Dependencies

        fun graph(): GraphProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = AccountsProjector::class,
                identifier = "accounts",
            ),
            Variant(
                type = GraphProjector::class,
                identifier = "graph",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "AnalyticsTabProjector",
    )
    interface TabProjector {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val tabs: AnalyticsTabValues<AnalyticsTabProjector> = AnalyticsTabValues(
        accounts = TabProjector.accounts(
            model = model.accounts,
            dependencies = dependencies.accounts(),
        ),
        graph = TabProjector.graph(
            scope = scope,
            model = model.graph,
            dependencies = dependencies.graph(),
        )
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val selectedTab by model.selectedTab.collectAsState()
        Overcompose(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            top = { contentPadding ->
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(
                            bottom = Dimens.separation,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Tabs(
                        items = remember { AnalyticsTab.entries.toList() },
                        selected = selectedTab,
                        onSelectedChanged = { model.selectedTab.value = it },
                    ) { tab ->
                        Text(
                            text = tab.title(
                                localization = dependencies.localization,
                            ),
                        )
                    }
                }
            },
        ) { contentPadding ->
            HorizontalPager(
                state = rememberPagerState(model.selectedTab),
            ) { i ->
                val tab = AnalyticsTab.entries[i]
                tabs[tab].Content(
                    contentPadding = contentPadding,
                )
            }
        }
    }
}