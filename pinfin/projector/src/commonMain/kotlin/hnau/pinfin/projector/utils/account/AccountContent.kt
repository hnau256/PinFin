package hnau.pinfin.projector.utils.account

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.utils.Icon
import hnau.pinfin.data.repository.AccountInfo

@Composable
fun AccountContent(
    info: AccountInfo,
    modifier: Modifier = Modifier,
) {
    TripleRow(
        modifier = modifier,
        content = { Text(info.title) },
        leading = { Icon { Icons.Filled.Wallet } },
    )
}