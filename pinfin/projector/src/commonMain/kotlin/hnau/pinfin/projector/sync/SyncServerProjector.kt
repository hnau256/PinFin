package hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.AlertDialogContent
import hnau.common.compose.uikit.ErrorPanel
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.NavigationIcon
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.no
import hnau.pinfin.projector.stop
import hnau.pinfin.projector.stop_sync_server
import hnau.pinfin.projector.sync_server
import hnau.pinfin.projector.sync_server_is_active
import hnau.pinfin.projector.yes
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class SyncServerProjector(
    scope: CoroutineScope,
    private val model: SyncServerModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies.globalGoBackHandler.resolve(scope)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.sync_server)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
            },
        ) { contentPadding ->
            ErrorPanel(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            stringResource(Res.string.sync_server_is_active),
                        )
                    }
                },
                button = {
                    Button(
                        onClick = model::stopServer,
                    ) {
                        Text(stringResource(Res.string.stop))
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
                title = { Text(stringResource(Res.string.stop_sync_server)) },
                confirmButton = {
                    TextButton(
                        onClick = model::confirmStopServerDialog,
                        content = { Text(stringResource(Res.string.yes)) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::cancelStopServerDialog,
                        content = { Text(stringResource(Res.string.no)) },
                    )
                }
            )
        }
    }
}