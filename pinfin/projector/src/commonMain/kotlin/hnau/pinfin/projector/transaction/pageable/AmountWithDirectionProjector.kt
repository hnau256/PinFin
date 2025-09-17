package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.pageable.AmountWithDirectionModel
import hnau.pinfin.projector.transaction.utils.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.icon
import hnau.pipe.annotations.Pipe

class AmountWithDirectionProjector(
    private val model: AmountWithDirectionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun amount(): AmountProjector.Dependencies
    }

    private val amount = AmountProjector(
        dependencies = dependencies.amount(),
        model = model.amountModel,
    )

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            val direction by model.direction.collectAsState()
            SwitchHueToAmountDirection(
                amountDirection = direction,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { model.switchDirection() },
                    ) {
                        Icon(
                            icon = direction.icon,
                        )
                    }
                    amount.InnerContent()
                }
            }
        }
    }
}