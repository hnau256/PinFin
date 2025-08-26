package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.pinfin.model.transaction.pageable.AmountModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.transaction.utils.AmountOrNullContent
import hnau.pinfin.projector.transaction_old_2.part.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.formatter.AmountFormatter
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
            delegate.Content(
                modifier = modifier.padding(contentPadding),
            )
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
            AmountOrNullContent(
                modifier = modifier,
                amount = model
                    .amount
                    .collectAsState()
                    .value,
                formatter = dependencies.amountFormatter,
            )
        }
    }
}