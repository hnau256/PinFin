package hnau.pinfin.projector.transaction.pageable

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.pageable.AmountDirectionModel
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.icon
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class AmountDirectionProjector(
    scope: CoroutineScope,
    private val model: AmountDirectionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val direction by model.direction.collectAsState()
        SwitchHueToAmountDirection(
            amountDirection = direction,
        ) {
            IconButton(
                modifier = modifier,
                onClick = { model.switchDirection() },
            ) {
                Icon(
                    icon = direction.icon,
                )
            }
        }
    }
}