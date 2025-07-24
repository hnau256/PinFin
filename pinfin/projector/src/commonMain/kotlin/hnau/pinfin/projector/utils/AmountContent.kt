package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.pinfin.data.Amount
import hnau.pinfin.projector.utils.formatter.AmountFormatter


@Composable
fun AmountContent(
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = when {
            value.value >= 0 -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.error
        },
        text = remember(value) {
            val prefix =when {
                value.value >= 0 -> "+"
                else -> ""
            }
            prefix + amountFormatter.format(value)
        }
    )
}