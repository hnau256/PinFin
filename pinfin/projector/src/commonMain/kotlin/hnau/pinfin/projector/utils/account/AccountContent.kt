package hnau.pinfin.projector.utils.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.utils.SwitchHueToAccountInfo

@Composable
fun AccountContent(
    info: AccountInfo,
    modifier: Modifier = Modifier,
) {
    SwitchHueToAccountInfo(
        info = info,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
        ) {
            Icon(
                icon = Icons.Filled.Wallet,
            )
            Text(
                text = info.title,
                maxLines = 1,
            )
        }
    }
}