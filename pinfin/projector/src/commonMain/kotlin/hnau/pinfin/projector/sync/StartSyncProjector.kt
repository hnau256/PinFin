package hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.TopBarTitle
import hnau.common.app.projector.uikit.progressindicator.InProgress
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.table.TableScope
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.plus
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.model.sync.start.StartSyncModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.address
import hnau.pinfin.projector.resources.budgets_sync
import hnau.pinfin.projector.resources.open_client
import hnau.pinfin.projector.resources.port
import hnau.pinfin.projector.resources.start_server
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.Button as MaterialButton

class StartSyncProjector(
    scope: CoroutineScope,
    private val model: StartSyncModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
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
                    TopBarTitle { Text(stringResource(Res.string.budgets_sync)) }
                }
            },
        ) { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding + PaddingValues(
                    horizontal = Dimens.horizontalDisplayPadding,
                    vertical = Dimens.verticalDisplayPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(
                    key = "server"
                ) {
                    Server()
                }
                item(
                    key = "client"
                ) {
                    Client()
                }
            }
            InProgress(model.inProgress)
        }
    }

    @Composable
    private fun Server() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            PortInput(
                isLast = false,
                onDone = { model.startServer.value?.invoke() },
            )
            Button(
                isLast = true,
                onClick = model.startServer,
                title = { stringResource(Res.string.start_server) },
            )
        }
    }

    @Composable
    private fun Client() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Input(
                isLast = false,
                title = { stringResource(Res.string.address) },
            ) {
                Cell(
                    isLast = true,
                ) {modifier ->
                    TextInput(
                        modifier = modifier,
                        maxLines = 1,
                        value = model.serverAddressInput,
                        isError = !model.serverAddressIsCorrect.collectAsState().value,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Uri,
                        ),
                        shape = shape,
                        placeholder = {
                            model.serverAddressPlaceholder?.address?.toString()
                                ?.let { placeholder ->
                                    Text(placeholder)
                                }
                        },
                    )
                }
            }
            PortInput(
                isLast = false,
                onDone = { model.openClient.value?.invoke() },
            )
            Button(
                isLast = true,
                onClick = model.openClient,
                title = { stringResource(Res.string.open_client) },
            )
        }
    }

    @Composable
    private fun TableScope.Button(
        isLast: Boolean,
        title: @Composable () -> String,
        onClick: StateFlow<(() -> Unit)?>,
    ) {
        Cell(
            isLast = isLast,
        ) { modifier ->
            val onClick by onClick.collectAsState()
            MaterialButton(
                modifier = modifier,
                shape = shape,
                content = { Text(title()) },
                onClick = { onClick?.invoke() },
                enabled = onClick != null,
            )
        }
    }

    @Composable
    private fun TableScope.Input(
        isLast: Boolean,
        title: @Composable () -> String,
        input: @Composable TableScope.() -> Unit,
    ) {
        Subtable(
            isLast = isLast,
        ) {
            CellBox(
                isLast = false,
                configModifier = { modifier -> modifier.width(96.dp) },
            ) {
                Text(
                    text = title(),
                    modifier = Modifier.padding(
                        horizontal = Dimens.smallSeparation,
                    )
                )
            }
            input()
        }
    }

    @Composable
    private fun TableScope.PortInput(
        isLast: Boolean,
        onDone: () -> Unit,
    ) {
        Input(
            isLast = isLast,
            title = { stringResource(Res.string.port) },
        ) {
            Cell(
                isLast = true,
            ) { modifier ->
                TextInput(
                    maxLines = 1,
                    modifier = modifier.weight(1f),
                    value = model.portInput,
                    isError = !model.portIsCorrect.collectAsState().value,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number,
                    ),
                    keyboardActions = KeyboardActions { onDone() },
                    shape = shape,
                    placeholder = { Text(model.portPlaceholder.port.toString()) },
                )
            }
        }
    }
}