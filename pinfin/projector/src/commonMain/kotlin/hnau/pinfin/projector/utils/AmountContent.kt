package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.pinfin.data.utils.SignedAmount
import hnau.pinfin.data.dto.Amount


@Composable
fun AmountContent(
    sign: Boolean?,
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = when (sign) {
            true -> MaterialTheme.colorScheme.primary
            false -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.onSurface
        },
        text = remember(sign, value) {
            val prefix = when (sign) {
                true -> "+"
                false -> "-"
                null -> ""
            }
            prefix + amountFormatter.format(value)
        }
    )
}

@Composable
fun SignedAmountContent(
    amount: SignedAmount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    AmountContent(
        amountFormatter = amountFormatter,
        modifier = modifier,
        sign = amount.positive,
        value = amount.amount,
    )
}

@Composable
fun AmountContent(
    amount: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    AmountContent(
        amountFormatter = amountFormatter,
        modifier = modifier,
        sign = null,
        value = amount,
    )
}