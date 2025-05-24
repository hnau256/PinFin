package hnau.pinfin.projector.transaction.type.transfer

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.projector.uikit.state.StateContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.table.TableScope
import hnau.common.projector.uikit.table.cellShape
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.projector.utils.Icon
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.type.transfer.TransferModel
import hnau.pinfin.model.transaction.type.transfer.TransferSide
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.transaction.type.utils.ChooseAccountProjector
import hnau.pinfin.projector.utils.ArrowDirection
import hnau.pinfin.projector.utils.ArrowIcon
import hnau.pinfin.projector.utils.account.AccountButton
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TransferProjector(
    private val scope: CoroutineScope,
    private val model: TransferModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun chooseAccount(): ChooseAccountProjector.Dependencies

        fun amount(): AmountProjector.Dependencies
    }

    private val choose: StateFlow<ChooseAccountProjector?> = model
        .choose
        .mapWithScope(
            scope = scope,
        ) { stateScope, chooseModelOrNull ->
            chooseModelOrNull?.let { chooseModel ->
                ChooseAccountProjector(
                    scope = stateScope,
                    model = chooseModel,
                    dependencies = dependencies.chooseAccount(),
                )
            }
        }

    private val amount: AmountProjector = AmountProjector(
        scope = scope,
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    @Composable
    fun Content() {
        choose
            .collectAsState()
            .value
            .StateContent(
                label = "AccountsOrChoosing",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = { it != null }
            ) { chooseOrNull ->
                when (chooseOrNull) {
                    null -> DefaultContent()
                    else -> chooseOrNull.Content()
                }
            }
    }

    @Composable
    private fun TableScope.AccountCell(
        side: TransferSide,
    ) {
        Cell {
            val (account, onClick) = model.accounts[side]
            AccountButton(
                modifier = Modifier.weight(1f),
                info = account.collectAsState().value,
                onClick = onClick,
                shape = cellShape,
            )
        }
    }

    @Composable
    private fun DefaultContent() {
        Table(
            orientation = TableOrientation.Vertical,
        ) {
            Subtable {
                AccountCell(
                    side = TransferSide.From,
                )

                CellBox {
                    Icon(
                        modifier = Modifier.padding(
                            horizontal = Dimens.separation,
                        ),
                        icon = ArrowIcon[ArrowDirection.StartToEnd],
                    )
                }
                AccountCell(
                    side = TransferSide.To,
                )
            }
            amount.Content(
                scope = this,
            )
        }
    }
}