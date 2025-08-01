package hnau.pinfin.projector.transaction_old.type.entry

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction_old.type.entry.EntryModel
import hnau.pinfin.projector.transaction_old.type.utils.ChooseAccountProjector
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.AccountButton
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class EntryProjectorHeaderDelegate(
    scope: CoroutineScope,
    private val model: EntryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun chooseAccount(): ChooseAccountProjector.Dependencies

        val amountFormatter: AmountFormatter
    }

    private val chooseAccount: StateFlow<ChooseAccountProjector?> = model
        .chooseAccount
        .mapWithScope(
            scope = scope,
        ) { stateScope, chooseAccountModelOrNull ->
            chooseAccountModelOrNull?.let { chooseAccountModel ->
                ChooseAccountProjector(
                    scope = stateScope,
                    model = chooseAccountModel,
                    dependencies = dependencies.chooseAccount(),
                )
            }
        }

    @Composable
    fun Content() {
        chooseAccount
            .collectAsState()
            .value
            .NullableStateContent(
                label = "ChooseAccountOrDefault",
                transitionSpec = TransitionSpec.vertical(),
                anyContent = { it.Content() },
                nullContent = {
                    Table(
                        orientation = TableOrientation.Horizontal,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Cell(
                            isLast = false,
                        ) {modifier ->
                            AccountButton(
                                modifier = modifier.weight(1f),
                                info = model.account.collectAsState().value,
                                onClick = model::chooseAccount,
                                shape = shape,
                            )
                        }
                        CellBox(
                            isLast = true,
                        ) {

                            val amount = model
                                .amount
                                .collectAsState()
                                .value

                            AmountContent(
                                value = amount,
                                modifier = Modifier.padding(
                                    horizontal = Dimens.separation,
                                ),
                                amountFormatter = dependencies.amountFormatter,
                            )
                        }
                    }
                }
            )
    }
}