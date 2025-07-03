package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import hnau.common.projector.utils.Overcompose
import hnau.common.projector.utils.copy
import hnau.common.projector.utils.rememberPagerState
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTabValues
import hnau.pipe.annotations.Pipe
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
    }

    private val tabs: AnalyticsTabValues<AnalyticsTabProjector> = AnalyticsTabValues(
        accounts = AnalyticsTabProjector.Accounts(
            AccountsProjector(
                scope = scope,
                model = model.accounts,
                dependencies = dependencies.accounts(),
            )
        ),
        categories = AnalyticsTabProjector.Categories(
            CategoriesProjector(
                scope = scope,
                model = model.categories,
                dependencies = dependencies.categories(),
            )
        )
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val selectedTab by model.selectedTab.collectAsState()
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            top = {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                ) {
                    AnalyticsTab.entries.fastForEach { tab ->
                        Tab(
                            selected = tab == selectedTab,
                            onClick = { model.selectedTab.value = tab },
                            text = {
                                Text(
                                    modifier = Modifier.padding(
                                        top = contentPadding.calculateTopPadding(),
                                    ),
                                    text = tab.title,
                                )
                            },
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
                    contentPadding = contentPadding.copy(
                        top = tabContentPadding.calculateTopPadding(),
                    )
                )
            }
        }
    }
}