package hnau.pinfin.projector.transaction.type.transfer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.table.TableScope
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.type.transfer.TransferModel
import hnau.pinfin.model.transaction.type.transfer.TransferSide
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.transaction.type.utils.ChooseAccountProjector
import hnau.pinfin.projector.utils.ArrowDirection
import hnau.pinfin.projector.utils.ArrowIcon
import hnau.pinfin.projector.utils.account.AccountButton
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
        isLast: Boolean,
        side: TransferSide,
    ) {
        Cell(
            isLast = isLast,
        ) { modifier ->
            val (account, onClick) = model.accounts[side]
            AccountButton(
                modifier = modifier.weight(1f),
                info = account.collectAsState().value,
                onClick = onClick,
                shape = shape,
            )
        }
    }

    @Composable
    private fun DefaultContent() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Subtable(
                isLast = false,
            ) {
                AccountCell(
                    isLast = false,
                    side = TransferSide.From,
                )
                CellBox(
                    isLast = false,
                ) {
                    Icon(
                        modifier = Modifier.padding(
                            horizontal = Dimens.separation,
                        ),
                        icon = ArrowIcon[ArrowDirection.StartToEnd],
                    )
                }
                AccountCell(
                    isLast = true,
                    side = TransferSide.To,
                )
            }
            Cell(
                isLast = true,
            ) { modifier ->
                amount.Content(
                    modifier = modifier,
                )
            }
        }
    }
}