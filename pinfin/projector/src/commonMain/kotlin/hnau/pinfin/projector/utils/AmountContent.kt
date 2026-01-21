package hnau.pinfin.projector.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import hnau.common.app.projector.utils.SwitchHue
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.model
import hnau.pinfin.projector.utils.formatter.AmountFormatter

val AmountDirection.icon: ImageVector
    get() = when (this) {
        AmountDirection.Credit -> Icons.Filled.AddCircle
        AmountDirection.Debit -> Icons.Filled.DoNotDisturbOn
    }

val AmountDirection.hue: Hue
    get() = when (this) {
        AmountDirection.Credit -> Hue(135)
        AmountDirection.Debit -> Hue(27)
    }

@Composable
fun SwitchHueToAmountDirection(
    amountDirection: AmountDirection,
    content: @Composable () -> Unit,
) {
    SwitchHue(
        hue = amountDirection.hue.model,
        content = content,
    )
}


@Composable
fun AmountContent(
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    if (value == Amount.zero) {
        AmountContentWithoutHue(
            value = value,
            amountFormatter = amountFormatter,
            modifier = modifier,
            style = style,
        )
        return
    }

    val (direction, _) = value.splitToDirectionAndRaw()
    SwitchHueToAmountDirection(
        amountDirection = direction,
    ) {
        AmountContentWithoutHue(
            value = value,
            amountFormatter = amountFormatter,
            modifier = modifier,
            style = style,
        )
    }
}

@Composable
private fun AmountContentWithoutHue(
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Text(
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.primary,
        text = remember(value) {
            amountFormatter.format(
                amount = value,
                alwaysShowSign = true,
            )
        }
    )
}