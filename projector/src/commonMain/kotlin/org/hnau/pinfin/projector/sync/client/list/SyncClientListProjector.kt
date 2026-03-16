package org.hnau.pinfin.projector.sync.client.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import arrow.core.NonEmptyList
import org.hnau.commons.app.projector.uikit.ErrorPanel
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.verticalDisplayPadding
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapReusable
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.sync.client.list.SyncClientListModel
import org.hnau.pinfin.projector.Res
import org.hnau.pinfin.projector.budgets_to_synchronization
import org.hnau.pinfin.projector.error_while_loading_budgets_list_from_server
import org.hnau.pinfin.projector.there_is_no_budgets_for_synchronization
import org.hnau.pinfin.projector.try_again
import org.hnau.pinfin.projector.utils.BackButtonWidth
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.pinfin.projector.Localization
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

class SyncClientListProjector(
    scope: CoroutineScope,
    private val model: SyncClientListModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization
    }

    private val items: StateFlow<Loadable<Result<NonEmptyList<Pair<BudgetId, SyncClientListItemProjector>>?>>> =
        model
            .items
            .mapReusable(scope) { itemsOrErrorOrLoading ->
                itemsOrErrorOrLoading.map { itemsOrError ->
                    itemsOrError.map { items ->
                        items?.map { (id, item) ->
                            getOrPutItem(id) {
                                val itemProjector = SyncClientListItemProjector(
                                    model = item,
                                )
                                id to itemProjector
                            }
                        }
                    }
                }
            }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        FullScreen(
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle { Text(dependencies.localization.budgetsToSynchronization) }
                }
            },
        ) { contentPadding ->
            items
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { budgetsOrError ->
                    budgetsOrError.fold(
                        onSuccess = { itemsOrEmpty ->
                            when (itemsOrEmpty) {
                                null -> EmptyState(
                                    contentPadding = contentPadding,
                                )

                                else -> Budgets(
                                    contentPadding = contentPadding,
                                    items = itemsOrEmpty,
                                )
                            }
                        },
                        onFailure = {
                            ErrorState(
                                contentPadding = contentPadding,
                            )
                        }
                    )
                }
        }
    }

    @Composable
    private fun EmptyState(
        contentPadding: PaddingValues,
    ) {
        ErrorPanel(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            title = { Text(dependencies.localization.thereIsNoBudgetsForSynchronization) },
        )
    }

    @Composable
    private fun ErrorState(
        contentPadding: PaddingValues,
    ) {
        ErrorPanel(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            title = { Text(dependencies.localization.errorWhileLoadingBudgetsListFromServer) },
            button = {
                Button(
                    onClick = model::reload,
                    content = { Text(dependencies.localization.tryAgain) },
                )
            },
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    private fun Budgets(
        contentPadding: PaddingValues,
        items: NonEmptyList<Pair<BudgetId, SyncClientListItemProjector>>,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = Dimens.horizontalDisplayPadding,
                    vertical = Dimens.verticalDisplayPadding,
                ),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            items(
                items = items,
                key = { it.first.id },
            ) { (_, itemProjector) ->
                itemProjector.Content()
            }
        }
    }
}