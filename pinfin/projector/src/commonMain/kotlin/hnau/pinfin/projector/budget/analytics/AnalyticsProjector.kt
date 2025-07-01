package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults.itemShape
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.projector.utils.Overcompose
import hnau.common.projector.utils.copy
import hnau.common.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.analytics.AnalyticsTabModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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

    private val tab: StateFlow<AnalyticsTabProjector> = model
        .tab
        .mapWithScope(scope) { tabScope, model ->
            when (model) {
                is AnalyticsTabModel.Accounts -> AnalyticsTabProjector.Accounts(
                    AccountsProjector(
                        scope = tabScope,
                        model = model.model,
                        dependencies = dependencies.accounts(),
                    )
                )

                is AnalyticsTabModel.Categories -> AnalyticsTabProjector.Categories(
                    CategoriesProjector(
                        scope = tabScope,
                        model = model.model,
                        dependencies = dependencies.categories(),
                    )
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val tab = tab.collectAsState().value
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            top = {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .padding(
                            top = contentPadding.calculateTopPadding(),
                        )
                        .horizontalDisplayPadding(),
                ) {
                    val selectedTab = tab.tab
                    AnalyticsTab
                        .entries
                        .fastForEachIndexed { i, tab ->
                            SegmentedButton(
                                selected = tab == selectedTab,
                                onClick = { model.selectTab(tab) },
                                shape = itemShape(
                                    index = i,
                                    count = AnalyticsTab.entries.size,
                                ),
                            ) {
                                Text(
                                    text = tab.title,
                                )
                            }
                        }
                }
            },
        ) { tabContentPadding ->
            tab.Content(
                contentPadding = contentPadding.copy(
                    top = tabContentPadding.calculateTopPadding(),
                )
            )
        }
    }
}