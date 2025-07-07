package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.utils.budget.state.SignedAmount
import hnau.pinfin.projector.utils.formatter.AmountFormatter


@Composable
fun AmountContent(
    direction: AmountDirection?,
    value: Amount,
    amountFormatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = when (direction) {
            AmountDirection.Credit -> MaterialTheme.colorScheme.primary
            AmountDirection.Debit -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.onSurface
        },
        text = remember(direction, value) {
            val prefix =when (direction) {
                AmountDirection.Credit -> "+"
                AmountDirection.Debit -> "-"
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
        direction = amount.direction,
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
        direction = null,
        value = amount,
    )
}