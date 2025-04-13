package hnau.pinfin.client.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.scheme.TransactionType
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.entry
import pinfin.pinfin.client.projector.generated.resources.transfer

val TransactionType.title: String
    @Composable
    get() = stringResource(
        when (this) {
            TransactionType.Entry -> Res.string.entry
            TransactionType.Transfer -> Res.string.transfer
        }
    )