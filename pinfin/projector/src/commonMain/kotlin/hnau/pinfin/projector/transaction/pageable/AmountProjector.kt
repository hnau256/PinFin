package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.copy
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.transaction.pageable.AmountModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.transaction.utils.AmountOrNullContent
import hnau.pinfin.projector.transaction_old_2.part.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.icon
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class AmountProjector(
    scope: CoroutineScope,
    private val model: AmountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    class Page(
        scope: CoroutineScope,
        private val model: AmountModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun delegate(): AmountProjector.Dependencies
        }

        private val delegate: AmountProjector = AmountProjector(
            scope = scope,
            dependencies = dependencies.delegate(),
            model = model.delegate,
        )

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            Column(
                modifier = modifier.horizontalDisplayPadding(),
            ) {
                delegate.Content(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding.copy(bottom = 0.dp)),
                )
            }
        }
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val direction by model.direction.collectAsState()
                SwitchHueToAmountDirection(
                    amountDirection = direction,
                ) {
                    IconButton(
                        onClick = { model.switchDirection() },
                    ) {
                        Icon(
                            icon = direction.icon,
                        )
                    }
                }
                AmountOrNullContent(
                    modifier = Modifier.weight(1f),
                    amount = model
                        .amount
                        .collectAsState()
                        .value,
                    formatter = dependencies.amountFormatter,
                )
            }
        }
    }
}