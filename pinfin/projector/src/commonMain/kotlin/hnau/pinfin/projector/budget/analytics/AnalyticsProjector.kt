package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
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
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.rememberPagerState
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTabValues
import hnau.pinfin.projector.budget.analytics.graph.GraphProjector
import hnau.pinfin.projector.utils.Tabs
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
class AnalyticsProjector(
    scope: CoroutineScope,
    private val model: AnalyticsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun accounts(): AccountsProjector.Dependencies

        fun categories(): CategoriesProjector.Dependencies

        fun graph(): GraphProjector.Dependencies
    }

    private val tabs: AnalyticsTabValues<AnalyticsTabProjector> = AnalyticsTabValues(
        accounts = AnalyticsTabProjector.Accounts(
            AccountsProjector(
                model = model.accounts,
                dependencies = dependencies.accounts(),
            )
        ),
        categories = AnalyticsTabProjector.Categories(
            CategoriesProjector(
                model = model.categories,
                dependencies = dependencies.categories(),
            )
        ),
        graph = AnalyticsTabProjector.Graph(
            GraphProjector(
                scope = scope,
                model = model.graph,
                dependencies = dependencies.graph(),
            )
        )
    )

    @Composable
    fun Content(
        bottomInset: Dp,
    ) {
        val selectedTab by model.selectedTab.collectAsState()
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            top = {
                Box(
                    modifier = Modifier
                        .statusBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Tabs(
                        items = remember { AnalyticsTab.entries.toImmutableList() },
                        selected = selectedTab,
                        onSelectedChanged = { model.selectedTab.value = it },
                    ) { tab ->
                        Text(
                            text = tab.title,
                        )
                    }
                }
            },
        ) { tabContentPadding ->
            HorizontalPager(
                state = rememberPagerState(model.selectedTab),
            ) { i ->
                val tab = AnalyticsTab.entries[i]
                tabs[tab].Content(
                    contentPadding = PaddingValues(
                        top = tabContentPadding.calculateTopPadding(),
                        bottom = bottomInset,
                    )
                )
            }
        }
    }
}