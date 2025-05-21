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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.model.goback.GoBackHandler
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.progressindicator.InProgress
import hnau.common.compose.uikit.table.CellBox
import hnau.common.compose.uikit.table.CellScope
import hnau.common.compose.uikit.table.Subtable
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.table.TableScope
import hnau.common.compose.uikit.table.cellShape
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.NavigationIcon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.common.compose.utils.plus
import hnau.common.compose.utils.verticalDisplayPadding
import hnau.pinfin.model.sync.start.StartSyncModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.address
import hnau.pinfin.projector.budgets_sync
import hnau.pinfin.projector.open_client
import hnau.pinfin.projector.port
import hnau.pinfin.projector.start_server
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class StartSyncProjector(
    scope: CoroutineScope,
    private val model: StartSyncModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.budgets_sync)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
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
                title = stringResource(Res.string.start_server),
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
                title = stringResource(Res.string.address),
            ) {
                TextInput(
                    value = model.serverAddressInput,
                    isError = !model.serverAddressIsCorrect.collectAsState().value,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Uri,
                    ),
                    shape = cellShape,
                    placeholder = {
                        model.serverAddressPlaceholder?.address?.toString()?.let { placeholder ->
                            Text(placeholder)
                        }
                    },
                )
            }
            PortInput(
                onDone = { model.openClient.value?.invoke() },
            )
            Button(
                onClick = model.openClient,
                title = stringResource(Res.string.open_client),
            )
        }
    }

    @Composable
    private fun TableScope.Button(
        title: String,
        onClick: StateFlow<(() -> Unit)?>,
    ) {
        Cell {
            HnauButton(
                shape = cellShape,
                content = { Text(title) },
                onClick = onClick.collectAsState().value,
            )
        }
    }

    @Composable
    private fun TableScope.Input(
        title: String,
        input: @Composable CellScope.() -> Unit,
    ) {
        Subtable {
            CellBox(
                modifier = Modifier.width(96.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(
                        horizontal = Dimens.smallSeparation,
                    )
                )
            }
            Cell(
                content = input,
            )
        }
    }

    @Composable
    private fun TableScope.PortInput(
        onDone: () -> Unit,
    ) {
        Input(
            title = stringResource(Res.string.port),
        ) {
            TextInput(
                value = model.portInput,
                isError = !model.portIsCorrect.collectAsState().value,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number,
                ),
                keyboardActions = KeyboardActions { onDone() },
                shape = cellShape,
                placeholder = { Text(model.portPlaceholder.port.toString()) },
            )
        }
    }
}