package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.app.model.goback.GlobalGoBackHandler
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.Separator
import hnau.common.app.projector.uikit.progressindicator.InProgress
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.NavigationIcon
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.transaction.type.TransactionTypeModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.new_transaction
import hnau.pinfin.projector.resources.transaction
import hnau.pinfin.projector.transaction.type.TransactionTypeProjector
import hnau.pinfin.projector.transaction.type.entry.EntryProjector
import hnau.pinfin.projector.transaction.type.transfer.TransferProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class TransactionProjector(
    private val scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun entry(): EntryProjector.Dependencies

        fun transfer(): TransferProjector.Dependencies

        fun mainInfoConfigDelegate(): TransactionProjectorMainInfoConfigDelegate.Dependencies

        fun mainInfoDateDelegate(): TransactionProjectorMainInfoDateDelegate.Dependencies

        fun mainInfoTimeDelegate(): TransactionProjectorMainInfoTimeDelegate.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val mainContent: StateFlow<Pair<Int, @Composable () -> Unit>> =
        model.mainContent.mapWithScope(scope) { stateScope, mainContent ->
            when (mainContent) {
                is TransactionModel.MainContent.Config -> TransactionProjectorMainInfoConfigDelegate(
                    scope = stateScope,
                    model = mainContent,
                    dependencies = dependencies.mainInfoConfigDelegate(),
                ).let { delegate ->
                    0 to { delegate.Content() }
                }

                is TransactionModel.MainContent.Date -> TransactionProjectorMainInfoDateDelegate(
                    scope = stateScope,
                    model = mainContent,
                    dependencies = dependencies.mainInfoDateDelegate(),
                ).let { delegate ->
                    1 to { delegate.Content() }
                }

                is TransactionModel.MainContent.Time -> TransactionProjectorMainInfoTimeDelegate(
                    scope = stateScope,
                    model = mainContent,
                    dependencies = dependencies.mainInfoTimeDelegate(),
                ).let { delegate ->
                    2 to { delegate.Content() }
                }
            }
        }

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
                    .verticalDisplayPadding()
                    .imePadding()
            ) {
                mainContent
                    .collectAsState()
                    .value
                    .StateContent(
                        label = "TransactionMainContent",
                        contentKey = Pair<Int, *>::first,
                        transitionSpec = TransitionSpec.vertical(),
                    ) { (_, content) ->
                        content()
                    }
                Separator()
                this@TransactionProjector.Type()
            }
            Dialogs(
                model = model,
            )
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
}