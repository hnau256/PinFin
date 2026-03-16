package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.projector.Res
import org.hnau.pinfin.projector.entry
import org.hnau.pinfin.projector.transfer
import org.jetbrains.compose.resources.stringResource

val TransactionType.title: String
    @Composable
    get() = stringResource(
        when (this) {
            TransactionType.Entry -> Res.string.entry
            TransactionType.Transfer -> Res.string.transfer
        }
    )