package hnau.pinfin.client.projector.budgetslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.ScreenContent
import hnau.common.compose.uikit.ScreenContentDependencies
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.common.compose.utils.plus
import hnau.common.compose.utils.verticalDisplayPadding
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.pinfin.client.model.budgetslist.BudgetsListModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.add

class BudgetsListProjector(
    scope: CoroutineScope,
    private val model: BudgetsListModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun item(): BudgetItemProjector.Dependencies

        fun screenContent(): ScreenContentDependencies
    }

    private val items: StateFlow<List<BudgetItemProjector>> = model
        .items
        .mapListReusable(
            scope = scope,
            extractKey = { it.id },
            transform = { itemScope, (_, model) ->
                BudgetItemProjector(
                    scope = itemScope,
                    model = model,
                    dependencies = dependencies.item(),
                )
            }
        )

    @Composable
    fun Content() {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title("Бюджеты")
            },
        ) { contentPadding ->
            val items by items.collectAsState()
            LazyColumn(
                contentPadding = contentPadding +
                        PaddingValues(Dimens.separation) +
                        PaddingValues(bottom = 96.dp),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                items(items) { item ->
                    item.Content()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .horizontalDisplayPadding()
                    .verticalDisplayPadding(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                ExtendedFloatingActionButton(
                    onClick = model.onAddBudgetClick,
                    icon = { Icon { Icons.Filled.Add } },
                    text = { Text(stringResource(Res.string.add)) },
                )
            }
        }
    }
}