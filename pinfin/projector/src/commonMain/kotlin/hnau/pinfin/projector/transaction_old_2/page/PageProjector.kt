package hnau.pinfin.projector.transaction_old_2.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed interface PageProjector {

    @Composable
    fun Content(
        modifier: Modifier,
        contentPadding: PaddingValues,
    )
}