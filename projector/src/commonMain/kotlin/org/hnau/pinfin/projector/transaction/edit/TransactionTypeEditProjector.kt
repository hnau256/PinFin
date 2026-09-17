package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import arrow.core.nonEmptyListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.transaction.edit.TransactionTypeEditModel
import org.hnau.pinfin.model.transaction.edit.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.title

class TransactionTypeEditProjector(
    scope: CoroutineScope,
    private val model: TransactionTypeEditModel,
    currency: StateFlow<Currency>,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun entry(): TransactionEntryEditProjector.Dependencies

        fun transfer(): TransactionTransferEditProjector.Dependencies
    }

    @Fold
    sealed interface Value {

        data class Entry(
            val projector: TransactionEntryEditProjector,
        ) : Value

        data class Transfer(
            val projector: TransactionTransferEditProjector,
        ) : Value
    }

    val value: StateFlow<Value> = model
        .type
        .mapWithScope(scope) { scope, typeModel ->
            typeModel.fold(
                ifEntry = { entryModel ->
                    Value.Entry(
                        projector = TransactionEntryEditProjector(
                            scope = scope,
                            model = entryModel,
                            currency = currency,
                            dependencies = dependencies.entry(),
                        ),
                    )
                },
                ifTransfer = { transferModel ->
                    Value.Transfer(
                        projector = TransactionTransferEditProjector(
                            model = transferModel,
                            dependencies = dependencies.transfer(),
                        ),
                    )
                },
            )
        }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val variant by model.typeVariant.collectAsState()
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(dependencies.localization.type)
            },
        ) {
            STabs(
                items = nonEmptyListOf(
                    TransactionType.Entry,
                    TransactionType.Transfer,
                ),
                getSelection = { variant },
                onSelectionChanged = model::setType,
            ) { type ->
                SText(type.title(dependencies.localization))
            }
        }
    }
}
