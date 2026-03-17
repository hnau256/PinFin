package org.hnau.pinfin.projector.budgetslist

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.ErrorPanel
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarAction
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.progressindicator.InProgress
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.app.projector.utils.verticalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapReusable
import org.hnau.pinfin.model.budgetslist.BudgetsListModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth

class BudgetsListProjector(
    scope: CoroutineScope,
    private val model: BudgetsListModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization
    }

    private val items: StateFlow<NonEmptyList<BudgetItemProjector>?> = model
        .items
        .mapReusable(scope) { itemsOrNull ->
            itemsOrNull?.map { item ->
                getOrPutItem(item.id) {
                    BudgetItemProjector(
                        model = item.model,
                    )
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        FullScreen(
            contentPadding = contentPadding,
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle {
                        Text((dependencies.localization.budgets))
                    }
                    TopBarAction(
                        onClick = model::openSync,
                    ) {
                        Icon(Icons.Filled.Sync)
                    }
                }
            },
        ) { contentPadding ->
            items.collectAsState().value.NullableStateContent(
                transitionSpec = TransitionSpec.crossfade(),
                nullContent = {
                    ErrorPanel(
                        title = { Text(dependencies.localization.noBudgets) },
                        button = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Button(
                                    onClick = model::createNewBudget,
                                ) {
                                    Text(dependencies.localization.createNewBudget)
                                }
                                OutlinedButton(
                                    onClick = model::openSync,
                                ) {
                                    Text(dependencies.localization.budgetsSync)
                                }
                                OutlinedButton(
                                    onClick = model::createDemoBudget,
                                ) {
                                    Text(dependencies.localization.createDemoBudget)
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
                        items(
                            items = items,
                        ) { item ->
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
                            text = { Text((dependencies.localization.add)) },
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