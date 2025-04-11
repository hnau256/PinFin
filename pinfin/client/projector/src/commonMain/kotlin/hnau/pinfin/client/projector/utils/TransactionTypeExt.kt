package hnau.pinfin.client.projector.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.scheme.TransactionType

//TODO("ComposeForAndroid")
val TransactionType.title: String
    @Composable
    get() = "QWERTY"/*stringResource(
        when (this) {
            TransactionType.Entry -> R.string.entry
            TransactionType.Transfer -> R.string.transfer
        }
    )*/