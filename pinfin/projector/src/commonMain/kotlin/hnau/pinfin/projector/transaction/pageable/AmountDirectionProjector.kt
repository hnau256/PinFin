package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            Button(
                modifier = modifier,
                onClick = { model.switchDirection() },
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    icon = direction.icon,
                )
            }
        }
    }
}