package hnau.pinfin.client.projector.transaction

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.Separator
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.NavigationIcon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.client.model.transaction.TransactionModel
import hnau.pinfin.client.model.transaction.type.TransactionTypeModel
import hnau.pinfin.client.projector.transaction.type.TransactionTypeProjector
import hnau.pinfin.client.projector.transaction.type.entry.EntryProjector
import hnau.pinfin.client.projector.transaction.type.transfer.TransferProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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
                    title = { Text("Транзакция") },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                    actions = {
                        SaveAction()
                        RemoveAction()
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
                false -> Icon { Icons.Filled.Save }
            }
        }
    }

    @Composable
    private fun RowScope.RemoveAction() {
        val removeFlow = model.remove ?: return
        val remove by removeFlow.collectAsState()
        IconButton(
            enabled = remove != null,
            onClick = { remove?.invoke() },
        ) {
            when (remove) {
                null -> CircularProgressIndicator()
                else -> Icon { Icons.Filled.Delete }
            }
        }
    }
}