package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.data.TransactionType
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.entry
import hnau.pinfin.projector.resources.transfer
import org.jetbrains.compose.resources.stringResource

val TransactionType.title: String
    @Composable
    get() = stringResource(
        when (this) {
            TransactionType.Entry -> Res.string.entry
            TransactionType.Transfer -> Res.string.transfer
        }
    )