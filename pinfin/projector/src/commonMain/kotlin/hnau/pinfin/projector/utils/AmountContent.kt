package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.projector.utils.formatter.AmountFormatter


@Composable
fun AmountContent(
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    val (direction, _) = value.splitToDirectionAndRaw()
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = direction.containerStyle.rememberColors().single,
        text = remember(value) {
            val prefix = when (direction) {
                AmountDirection.Credit -> "+"
                AmountDirection.Debit -> ""
            }
            prefix + amountFormatter.format(value)
        }
    )
}