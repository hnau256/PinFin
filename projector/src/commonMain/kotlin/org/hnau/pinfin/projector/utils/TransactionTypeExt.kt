package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.projector.Localization


@Composable
fun TransactionType.title(
    localization: Localization,
): String = fold(
    ifEntry = { localization.entry },
    ifTransfer = { localization.transfer },
)