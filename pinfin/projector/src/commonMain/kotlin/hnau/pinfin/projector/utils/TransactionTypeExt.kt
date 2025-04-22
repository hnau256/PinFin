package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.data.dto.TransactionType
import org.jetbrains.compose.resources.stringResource
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.entry
import hnau.pinfin.projector.transfer

val TransactionType.title: String
    @Composable
    get() = stringResource(
        when (this) {
            TransactionType.Entry -> Res.string.entry
            TransactionType.Transfer -> Res.string.transfer
        }
    )