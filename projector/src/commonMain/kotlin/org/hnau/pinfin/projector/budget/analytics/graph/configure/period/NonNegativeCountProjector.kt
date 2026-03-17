package org.hnau.pinfin.projector.budget.analytics.graph.configure.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.NonNegativeCountModel

class NonNegativeCountProjector(
    private val model: NonNegativeCountModel,
    private val title: String,
) {

    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            ShiftButton(
                title = "-",
                onClick = model.dec,
            )
            TextInput(
                modifier = Modifier.weight(1f),
                value = model.manual,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                maxLines = 1,
                label = { Text(title) },
                isError = model.isCorrect.collectAsState().value.not(),
            )
            ShiftButton(
                title = "+",
                onClick = model.inc,
            )
        }
    }

    @Composable
    private fun ShiftButton(
        title: String,
        onClick: StateFlow<(() -> Unit)?>,
    ) {
        val onClick by onClick.collectAsState()
        OutlinedButton(
            onClick = { onClick?.invoke() },
            enabled = onClick != null,
        ) {
            Text(title)
        }
    }
}