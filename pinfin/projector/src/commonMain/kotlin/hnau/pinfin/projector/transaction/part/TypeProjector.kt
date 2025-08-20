package hnau.pinfin.projector.transaction.part

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction_old_2.part.TypeModel
import hnau.pinfin.model.transaction_old_2.part.type.EntryModel
import hnau.pinfin.model.transaction_old_2.part.type.TransferModel
import hnau.pinfin.projector.transaction.part.type.EntryProjector
import hnau.pinfin.projector.transaction.part.type.PartTypeProjector
import hnau.pinfin.projector.transaction.part.type.TransferProjector
import hnau.pinfin.projector.utils.Tabs
import hnau.pinfin.projector.utils.title
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TypeProjector(
    scope: CoroutineScope,
    private val model: TypeModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun entry(): EntryProjector.Dependencies

        fun transfer(): TransferProjector.Dependencies
    }

    @Composable
    fun HeaderContent(
        modifier: Modifier = Modifier,
    ) {
        Tabs(
            modifier = modifier,
            items = remember { TransactionType.entries.toImmutableList() },
            selected = model.typeVariant.collectAsState().value,
            onSelectedChanged = model::setVariant,
        ) { type ->
            Text(
                text = type.title,
            )
        }
    }

    private val type: StateFlow<PartTypeProjector> = model
        .type
        .mapWithScope(scope) { typeScope, model ->
            when (model) {
                is EntryModel -> EntryProjector(
                    scope = typeScope,
                    model = model,
                    dependencies = dependencies.entry(),
                )

                is TransferModel -> TransferProjector(
                    scope = typeScope,
                    model = model,
                    dependencies = dependencies.transfer(),
                )
            }
        }

    private val PartTypeProjector.key: Int
        get() = when (this) {
            is EntryProjector -> 0
            is TransferProjector -> 1
        }

    @Composable
    fun MainContent(
        modifier: Modifier = Modifier,
    ) {
        type
            .collectAsState()
            .value
            .StateContent(
                transitionSpec = TransitionSpec.crossfade(),
                label = "TransactionTypeMainContent",
                contentKey = { typeProjector -> typeProjector.key },
            ) { typeProjector ->
                typeProjector.MainContent(
                    modifier = modifier,
                )
            }
    }

    @Composable
    fun AmountContent(
        modifier: Modifier = Modifier,
    ) {
        type
            .collectAsState()
            .value
            .StateContent(
                transitionSpec = TransitionSpec.crossfade(),
                label = "TransactionTypeAmountContent",
                contentKey = { typeProjector -> typeProjector.key },
            ) { typeProjector ->
                typeProjector.AmountContent(
                    modifier = modifier,
                )
            }
    }
}