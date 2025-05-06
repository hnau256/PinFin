package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.AlertDialogContent
import hnau.common.compose.uikit.Separator
import hnau.common.compose.uikit.progressindicator.InProgress
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.NavigationIcon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.transaction.type.TransactionTypeModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.close
import hnau.pinfin.projector.new_transaction
import hnau.pinfin.projector.no
import hnau.pinfin.projector.not_save
import hnau.pinfin.projector.remove_transaction
import hnau.pinfin.projector.save
import hnau.pinfin.projector.save_changes
import hnau.pinfin.projector.transaction
import hnau.pinfin.projector.transaction.type.TransactionTypeProjector
import hnau.pinfin.projector.transaction.type.entry.EntryProjector
import hnau.pinfin.projector.transaction.type.transfer.TransferProjector
import hnau.pinfin.projector.yes
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class TransactionProjector(
    private val scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun entry(): EntryProjector.Dependencies

        fun transfer(): TransferProjector.Dependencies

        fun baseInfoDelegate(): TransactionProjectorBaseInfoDelegate.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val baseInfoDelegate = TransactionProjectorBaseInfoDelegate(
        scope = scope,
        model = model,
        dependencies = dependencies.baseInfoDelegate(),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                when (model.isNewTransaction) {
                                    true -> Res.string.new_transaction
                                    false -> Res.string.transaction
                                }
                            )
                        )
                    },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                    actions = {
                        RemoveAction()
                        SaveAction()
                    }
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .horizontalDisplayPadding()
                    .padding(vertical = Dimens.separation)
                    .padding(bottom = 96.dp)
            ) {
                baseInfoDelegate.Content()
                Separator()
                this@TransactionProjector.Type()
            }
            ExitUnsavedDialog()
            RemoveDialog()
            InProgress(model.inProgress)
        }
    }

    private val type: StateFlow<TransactionTypeProjector> = model
        .type
        .mapWithScope(
            scope = scope,
        ) { typeScope, type ->
            when (type) {
                is TransactionTypeModel.Entry -> TransactionTypeProjector.Entry(
                    projector = EntryProjector(
                        scope = typeScope,
                        model = type.model,
                        dependencies = dependencies.entry(),
                    )
                )

                is TransactionTypeModel.Transfer -> TransactionTypeProjector.Transfer(
                    projector = TransferProjector(
                        scope = typeScope,
                        model = type.model,
                        dependencies = dependencies.transfer(),
                    )
                )
            }
        }

    @Composable
    private fun Type() {
        type
            .collectAsState()
            .value
            .StateContent(
                label = "TypeOrChoosing",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = { it.key },
            ) { type ->
                type.Content()
            }
    }

    @Composable
    private fun RowScope.SaveAction() {
        val saveFlow by model.save.collectAsState()
        val save = saveFlow?.collectAsState()?.value
        val isSaving = saveFlow != null && save == null
        IconButton(
            enabled = save != null,
            onClick = { save?.invoke() },
        ) {
            when (isSaving) {
                true -> CircularProgressIndicator()
                false -> Icon(Icons.Filled.Save)
            }
        }
    }

    @Composable
    private fun RowScope.RemoveAction() {
        val remove = model.remove ?: return
        IconButton(
            onClick = remove,
        ) {
            Icon(Icons.Filled.Delete)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExitUnsavedDialog() {
        val info = model.exitUnsavedDialogInfo.collectAsState().value ?: return
        BasicAlertDialog(
            onDismissRequest = info.dismiss,
        ) {
            AlertDialogContent(
                title = { Text(stringResource(Res.string.save_changes)) },
                buttons = {
                    TextButton(
                        onClick = info.exitWithoutSaving,
                        content = { Text(stringResource(Res.string.not_save)) },
                    )
                    info.save.foldNullable(
                        ifNull = {
                            TextButton(
                                onClick = info.dismiss,
                                content = { Text(stringResource(Res.string.close)) },
                            )
                        },
                        ifNotNull = { save ->
                            TextButton(
                                onClick = save,
                                content = { Text(stringResource(Res.string.save)) },
                            )
                        }
                    )
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RemoveDialog() {
        val info = model.removeDialogInfo.collectAsState().value ?: return
        BasicAlertDialog(
            onDismissRequest = info.dismiss,
        ) {
            AlertDialogContent(
                title = { Text(stringResource(Res.string.remove_transaction)) },
                dismissButton = {
                    TextButton(
                        onClick = info.dismiss,
                        content = { Text(stringResource(Res.string.no)) },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = info.remove,
                        content = { Text(stringResource(Res.string.yes)) },
                    )
                }
            )
        }
    }
}