package hnau.pinfin.projector.sync.client.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import arrow.core.NonEmptyList
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.map
import hnau.common.app.model.goback.GlobalGoBackHandler
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.ErrorPanel
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.NavigationIcon
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.budgets_to_synchronization
import hnau.pinfin.projector.resources.error_while_loading_budgets_list_from_server
import hnau.pinfin.projector.resources.there_is_no_budgets_for_synchronization
import hnau.pinfin.projector.resources.try_again
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

class SyncClientListProjector(
    scope: CoroutineScope,
    private val model: SyncClientListModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler

        fun item(): SyncClientListItemProjector.Dependencies
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val items: StateFlow<Loadable<Result<NonEmptyList<Pair<BudgetId, SyncClientListItemProjector>>?>>> =
        model
            .items
            .mapReusable(scope) { itemsOrErrorOrLoading ->
                itemsOrErrorOrLoading.map { itemsOrError ->
                    itemsOrError.map { items ->
                        items?.map { (id, item) ->
                            getOrPutItem(id) { itemScope ->
                                val itemProjector = SyncClientListItemProjector(
                                    scope = itemScope,
                                    model = item,
                                    dependencies = dependencies.item(),
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.budgets_to_synchronization)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
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
            title = { Text(stringResource(Res.string.there_is_no_budgets_for_synchronization)) },
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
            title = { Text(stringResource(Res.string.error_while_loading_budgets_list_from_server)) },
            button = {
                Button(
                    onClick = model::reload,
                    content = { Text(stringResource(Res.string.try_again)) },
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