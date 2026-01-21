package hnau.pinfin.projector.budget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEach
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.budget.BudgetPageModel
import hnau.pinfin.model.budget.BudgetTab
import hnau.pinfin.model.budget.fold
import hnau.pinfin.model.budget.tab
import hnau.pinfin.projector.budget.analytics.AnalyticsProjector
import hnau.pinfin.projector.budget.config.BudgetConfigProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.analytics
import hnau.pinfin.projector.resources.config
import hnau.pinfin.projector.resources.transactions
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class BudgetProjector(
    scope: CoroutineScope,
    private val model: BudgetModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun transactions(): TransactionsProjector.Dependencies

        fun analytics(): AnalyticsProjector.Dependencies
    }
    
    @SealUp(
        variants = [
            Variant(
                type = TransactionsProjector::class,
                identifier = "transactions",
            ),
            Variant(
                type = AnalyticsProjector::class,
                identifier = "analytics",
            ),
            Variant(
                type = BudgetConfigProjector::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "BudgetPageProjector",
    )
    interface PageProjector {

        @Composable
        fun Content(
            bottomInset: Dp,
        )

        companion object
    }

    private val tabsCache: MutableMap<BudgetPageModel, Pair<BudgetTab, BudgetPageProjector>> =
        mutableMapOf()

    private val projector: StateFlow<Pair<BudgetTab, BudgetPageProjector>> = model
        .currentModel
        .mapState(scope) { model ->
            tabsCache.getOrPut(
                key = model,
            ) {
                val projector = model.fold(
                    ifTransactions = { transactionsModel ->
                        PageProjector.transactions(
                            scope = scope,
                            model = transactionsModel,
                            dependencies = dependencies.transactions(),
                        )
                    },
                    ifAnalytics = { analyticsModel ->
                        PageProjector.analytics(
                            scope = scope,
                            model = analyticsModel,
                            dependencies = dependencies.analytics(),
                        )
                    },
                    ifConfig = { budgetModel ->
                        PageProjector.config(
                            model = budgetModel,
                        )
                    }
                )
                model.tab to projector
            }
        }

    @Composable
    fun Content() {
        val tabWithProjector by projector.collectAsState()
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            bottom = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    val selectedTab = tabWithProjector.first
                    BudgetTab.entries.fastForEach { tab ->
                        NavigationBarItem(
                            modifier = Modifier.navigationBarsPadding(),
                            selected = tab == selectedTab,
                            icon = { Icon(tab.icon) },
                            label = { Text(tab.title) },
                            onClick = { model.selectTab(tab) },
                        )
                    }
                }
            }
        ) { contentPadding ->
            tabWithProjector.StateContent(
                modifier = Modifier.fillMaxSize(),
                contentKey = { (tab) -> tab },
                transitionSpec = getTransitionSpecForSlide(
                    orientation = SlideOrientation.Horizontal,
                ) {
                    when (targetState.first.ordinal > initialState.first.ordinal) {
                        true -> 1f
                        false -> -1f
                    } * 0.25f
                },
                label = "BudgetPage",
            ) { (_, projector) ->
                projector.Content(
                    bottomInset = contentPadding.calculateBottomPadding(),
                )
            }
        }
    }

    private val BudgetTab.icon: ImageVector
        get() = when (this) {
            BudgetTab.Transactions -> Icons.AutoMirrored.Filled.List
            BudgetTab.Analytics -> Icons.Filled.QueryStats
            BudgetTab.Config -> Icons.Filled.Settings
        }

    private val BudgetTab.title: String
        @Composable
        get() = stringResource(
            when (this) {
                BudgetTab.Transactions -> Res.string.transactions
                BudgetTab.Analytics -> Res.string.analytics
                BudgetTab.Config -> Res.string.config
            }
        )

}