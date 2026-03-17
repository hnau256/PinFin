package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.AlertDialogContent
import org.hnau.commons.app.projector.uikit.ErrorPanel
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.sync.server.SyncServerModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth

class SyncServerProjector(
    private val model: SyncServerModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization
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
                    TopBarTitle { Text(dependencies.localization.syncServer) }
                }
            },
        ) { contentPadding ->
            ErrorPanel(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(text = (dependencies.localization.syncServerIsActive))
                        Text(
                            text = model.addresses.joinToString(
                                separator = "\n",
                                prefix = dependencies.localization.addressesToConnect + ":\n",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                button = {
                    Button(
                        onClick = model::stopServer,
                    ) {
                        Text((dependencies.localization.stop))
                    }
                })
        }
        StopServerDialog()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun StopServerDialog() {
        val stopServerDialogIsOpened by model.stopServerDialogIsOpened.collectAsState()
        if (!stopServerDialogIsOpened) {
            return
        }
        BasicAlertDialog(
            onDismissRequest = model::cancelStopServerDialog
        ) {
            AlertDialogContent(
                title = { Text(dependencies.localization.stopSyncServer) },
                confirmButton = {
                    TextButton(
                        onClick = model::confirmStopServerDialog,
                        content = { Text((dependencies.localization.yes)) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::cancelStopServerDialog,
                        content = { Text((dependencies.localization.no)) },
                    )
                }
            )
        }
    }
}