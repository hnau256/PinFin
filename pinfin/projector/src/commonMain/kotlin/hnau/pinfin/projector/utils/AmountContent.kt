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
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.Hue
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
        hue = amountDirection.hue,
        content = content,
    )
}


@Composable
fun AmountContent(
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    val (direction, _) = value.splitToDirectionAndRaw()
    SwitchHueToAmountDirection(
        amountDirection = direction,
    ) {
        Text(
            modifier = modifier,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = remember(value) {
                val prefix = when (direction) {
                    AmountDirection.Credit -> "+"
                    AmountDirection.Debit -> ""
                }
                prefix + amountFormatter.format(value)
            }
        )
    }
}