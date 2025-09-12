package hnau.pinfin.projector.transaction.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.data.Amount
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.UIConstants
import hnau.pinfin.projector.utils.formatter.AmountFormatter

@Composable
fun AmountOrNullContent(
    amount: Amount?,
    formatter: AmountFormatter,
    modifier: Modifier = Modifier,
) {
    amount.NullableStateContent(
        modifier = modifier,
        transitionSpec = TransitionSpec.crossfade(),
        nullContent = {
            Icon(
                icon = UIConstants.absentValueIcon,
                tint = UIConstants.absentValueColor,
            )
        },
        anyContent = { amount ->
            AmountContent(
                value = amount,
                amountFormatter = formatter,
            )
        }
    )
}