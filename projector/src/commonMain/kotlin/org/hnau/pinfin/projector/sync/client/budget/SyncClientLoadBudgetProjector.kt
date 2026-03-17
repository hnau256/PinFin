package org.hnau.pinfin.projector.sync.client.budget

import androidx.compose.foundation.layout.PaddingValues
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.AlertDialogContent
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth

class SyncClientLoadBudgetProjector(
    scope: CoroutineScope,
    private val model: SyncClientLoadBudgetModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization

        fun syncClient(): SyncClientBudgetProjector.Dependencies
    }

    private val state: StateFlow<Loadable<SyncClientBudgetProjector>> = model
        .state
        .mapState(scope) { stateOrLoading ->
            stateOrLoading.map { state ->
                SyncClientBudgetProjector(
                    model = state,
                    dependencies = dependencies.syncClient(),
                )
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
                    TopBarTitle { Text(dependencies.localization.budgetSync) }
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
                title = { Text(dependencies.localization.stopSync) },
                confirmButton = {
                    TextButton(
                        onClick = model::stopSyncConfirm,
                        content = { Text((dependencies.localization.yes)) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::stopSyncCancel,
                        content = { Text((dependencies.localization.no)) },
                    )
                }
            )
        }
    }
}