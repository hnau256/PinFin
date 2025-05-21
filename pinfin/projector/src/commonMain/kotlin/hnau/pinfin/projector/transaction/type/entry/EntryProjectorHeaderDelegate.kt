package hnau.pinfin.projector.transaction.type.entry

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.projector.uikit.state.NullableStateContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.table.cellShape
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.type.entry.EntryModel
import hnau.pinfin.projector.transaction.type.utils.ChooseAccountProjector
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.SignedAmountContent
import hnau.pinfin.projector.utils.account.AccountButton
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class EntryProjectorHeaderDelegate(
    scope: CoroutineScope,
    private val model: EntryModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
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
                        Cell {
                            AccountButton(
                                modifier = Modifier.weight(1f),
                                info = model.account.collectAsState().value,
                                onClick = model::chooseAccount,
                                shape = cellShape,
                            )
                        }
                        val amount = model
                            .amount
                            .collectAsState()
                            .value
                        CellBox {
                            SignedAmountContent(
                                amount = amount,
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