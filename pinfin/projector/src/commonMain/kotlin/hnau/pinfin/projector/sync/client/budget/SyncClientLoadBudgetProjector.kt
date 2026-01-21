package hnau.pinfin.projector.sync.client.budget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.AlertDialogContent
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.TopBarTitle
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.map
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.budget_sync
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.stop_sync
import hnau.pinfin.projector.resources.yes
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class SyncClientLoadBudgetProjector(
    scope: CoroutineScope,
    private val model: SyncClientLoadBudgetModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
    }

    private val state: StateFlow<Loadable<SyncClientBudgetProjector>> = model
        .state
        .mapState(scope) { stateOrLoading ->
            stateOrLoading.map { state ->
                SyncClientBudgetProjector(
                    model = state,
                )
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
                    TopBarTitle { Text(stringResource(Res.string.budget_sync)) }
                }
            },
        ) { contentPadding ->
            state
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier
                        .fillMaxSize(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { budgetProjector ->
                    budgetProjector.Content(
                        contentPadding = contentPadding,
                    )
                }
            StopSyncDialog()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun StopSyncDialog() {
        val stopServerDialogIsOpened by model.isStopSyncDialogVisible.collectAsState()
        if (!stopServerDialogIsOpened) {
            return
        }
        BasicAlertDialog(
            onDismissRequest = model::stopSyncCancel
        ) {
            AlertDialogContent(
                title = { Text(stringResource(Res.string.stop_sync)) },
                confirmButton = {
                    TextButton(
                        onClick = model::stopSyncConfirm,
                        content = { Text(stringResource(Res.string.yes)) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::stopSyncCancel,
                        content = { Text(stringResource(Res.string.no)) },
                    )
                }
            )
        }
    }
}