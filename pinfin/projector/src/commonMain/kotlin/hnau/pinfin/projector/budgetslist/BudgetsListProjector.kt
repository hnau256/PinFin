package hnau.pinfin.projector.budgetslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.model.goback.GoBackHandler
import hnau.common.projector.uikit.ErrorPanel
import hnau.common.projector.uikit.progressindicator.InProgress
import hnau.common.projector.uikit.state.NullableStateContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.projector.utils.Icon
import hnau.common.projector.utils.NavigationIcon
import hnau.common.projector.utils.horizontalDisplayPadding
import hnau.common.projector.utils.plus
import hnau.common.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.coroutines.mapReusable
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.add
import hnau.pinfin.projector.resources.budgets
import hnau.pinfin.projector.resources.budgets_sync
import hnau.pinfin.projector.resources.create_demo_budget
import hnau.pinfin.projector.resources.create_new_budget
import hnau.pinfin.projector.resources.no_budgets
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class BudgetsListProjector(
    scope: CoroutineScope,
    private val model: BudgetsListModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun item(): BudgetItemProjector.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies.globalGoBackHandler.resolve(scope)

    private val items: StateFlow<NonEmptyList<BudgetItemProjector>?> =
        model.items.mapReusable(scope) { itemsOrNull ->
                itemsOrNull?.map { item ->
                    getOrPutItem(item.id) { itemProjectorScope ->
                        BudgetItemProjector(
                            scope = itemProjectorScope,
                            model = item.model,
                            dependencies = dependencies.item(),
                        )
                    }
                }
            }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.budgets)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                    actions = {
                        IconButton(
                            onClick = model::openSync,
                        ) {
                            Icon(Icons.Filled.Sync)
                        }
                    })
            },
        ) { contentPadding ->
            items.collectAsState().value.NullableStateContent(
                    transitionSpec = TransitionSpec.crossfade(),
                    nullContent = {
                        ErrorPanel(
                            title = { Text(stringResource(Res.string.no_budgets)) },
                            button = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Button(
                                        onClick = model::createNewBudget,
                                    ) {
                                        Text(stringResource(Res.string.create_new_budget))
                                    }
                                    OutlinedButton(
                                        onClick = model::openSync,
                                    ) {
                                        Text(stringResource(Res.string.budgets_sync))
                                    }
                                    OutlinedButton(
                                        onClick = model::createDemoBudget,
                                    ) {
                                        Text(stringResource(Res.string.create_demo_budget))
                                    }
                                }
                            })
                    },
                    anyContent = { items ->
                        LazyColumn(
                            contentPadding = contentPadding + PaddingValues(Dimens.separation) + PaddingValues(
                                bottom = 96.dp
                            ),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                        ) {
                            items(items) { item ->
                                item.Content()
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize().padding(contentPadding)
                                .horizontalDisplayPadding().verticalDisplayPadding(),
                            contentAlignment = Alignment.BottomEnd,
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = model::createNewBudget,
                                icon = { Icon(Icons.Filled.Add) },
                                text = { Text(stringResource(Res.string.add)) },
                            )
                        }
                    }
            )
            InProgress(
                inProgress = model.inProgress,
            )
        }
    }
}