package hnau.pinfin.client.projector.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.ScreenContent
import hnau.common.compose.uikit.ScreenContentDependencies
import hnau.common.compose.uikit.Separator
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.topappbar.TopAppBarScope
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
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
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.add
import pinfin.pinfin.client.projector.generated.resources.save

class TransactionProjector(
    private val scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun screenContent(): ScreenContentDependencies

        fun entry(): EntryProjector.Dependencies

        fun transfer(): TransferProjector.Dependencies

        fun baseInfoDelegate(): TransactionProjectorBaseInfoDelegate.Dependencies
    }

    private val baseInfoDelegate = TransactionProjectorBaseInfoDelegate(
        scope = scope,
        model = model,
        dependencies = dependencies.baseInfoDelegate(),
    )

    @Composable
    fun Content() {
        //TODO progress indicator
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title("Транзакция")
                saveButton()
            }
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
    private fun TopAppBarScope.saveButton() {
        val saveFlow by model.save.collectAsState()
        val save = saveFlow?.collectAsState()?.value
        val isSaving = saveFlow != null && save == null
        Action(
            onClick = save,
        ) {
            when (isSaving) {
                true -> CircularProgressIndicator()
                false -> Icon {
                    when (model.isNewTransaction) {
                        true -> Icons.Filled.Add
                        false -> Icons.Filled.Save
                    }
                }
            }
        }
    }
}