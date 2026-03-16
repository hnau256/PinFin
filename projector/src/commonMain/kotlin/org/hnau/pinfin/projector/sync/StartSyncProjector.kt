package org.hnau.pinfin.projector.sync

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
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.progressindicator.InProgress
import org.hnau.commons.app.projector.uikit.table.CellBox
import org.hnau.commons.app.projector.uikit.table.Subtable
import org.hnau.commons.app.projector.uikit.table.Table
import org.hnau.commons.app.projector.uikit.table.TableOrientation
import org.hnau.commons.app.projector.uikit.table.TableScope
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.app.projector.utils.verticalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.sync.start.StartSyncModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.budgets_sync
import org.hnau.pinfin.projector.open_client
import org.hnau.pinfin.projector.start_server
import org.hnau.pinfin.projector.utils.BackButtonWidth
import androidx.compose.material3.Button as MaterialButton

class StartSyncProjector(
    private val model: StartSyncModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization
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
                    TopBarTitle { Text(dependencies.localization.budgetsSync) }
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

                onDone = { model.startServer.value?.invoke() },
            )
            Button(

                onClick = model.startServer,
                title = { dependencies.localization.startServer },
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

                title = { (dependencies.localization.address) },
            ) {
                Cell(

                ) { modifier ->
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

                onDone = { model.openClient.value?.invoke() },
            )
            Button(

                onClick = model.openClient,
                title = { dependencies.localization.openClient },
            )
        }
    }

    @Composable
    private fun TableScope.Button(
        title: @Composable () -> String,
        onClick: StateFlow<(() -> Unit)?>,
    ) {
        Cell(
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
        title: @Composable () -> String,
        input: @Composable TableScope.() -> Unit,
    ) {
        Subtable {
            CellBox(

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
        onDone: () -> Unit,
    ) {
        Input(
            title = { (dependencies.localization.port) },
        ) {
            Cell(

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