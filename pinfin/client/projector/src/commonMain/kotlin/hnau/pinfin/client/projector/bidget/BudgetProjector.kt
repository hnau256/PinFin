package hnau.pinfin.client.projector.bidget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.util.fastForEach
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.client.model.budget.BudgetModel
import hnau.pinfin.client.model.budget.BudgetPageModel
import hnau.pinfin.client.model.budget.BudgetTab
import hnau.pinfin.client.projector.bidget.transactions.TransactionsProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.transactions

class BudgetProjector(
    scope: CoroutineScope,
    private val model: BudgetModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun transactions(): TransactionsProjector.Dependencies
    }

    private val tabsCache: MutableMap<BudgetPageModel, Pair<BudgetTab, BudgetPageProjector>> =
        mutableMapOf()

    private val projector: StateFlow<Pair<BudgetTab, BudgetPageProjector>> = model
        .currentModel
        .mapState(scope) { model ->
            tabsCache.getOrPut(
                key = model,
            ) {
                val projector = when (model) {
                    is BudgetPageModel.Transactions -> BudgetPageProjector.Transactions(
                        projector = TransactionsProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.transactions(),
                        )
                    )
                }
                model.tab to projector
            }
        }

    @Composable
    fun Content() {
        val tabWithProjector by projector.collectAsState()
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val selectedTab = tabWithProjector.first
                    BudgetTab.entries.fastForEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTab,
                            icon = { Icon { tab.icon } },
                            label = { Text(tab.title) },
                            onClick = { model.selectTab(tab) },
                        )
                    }
                }
            }
        ) { contentPadding ->
            tabWithProjector
                .StateContent(
                    modifier = Modifier.fillMaxSize(),
                    contentKey = { (tab) -> tab },
                    transitionSpec = getTransitionSpecForHorizontalSlide {
                        when (targetState.first.ordinal > initialState.first.ordinal) {
                            true -> 1f
                            false -> -1f
                        }
                    },
                    label = "BudgetPage",
                ) { (_, projector) ->
                    projector.Content(
                        contentPadding = contentPadding,
                    )
                }
        }
    }

    private val BudgetTab.icon: ImageVector
        get() = when (this) {
            BudgetTab.Transactions -> Icons.AutoMirrored.Filled.List
        }

    private val BudgetTab.title: String
        @Composable
        get() = stringResource(
            when (this) {
            BudgetTab.Transactions -> Res.string.transactions
        }
        )

}