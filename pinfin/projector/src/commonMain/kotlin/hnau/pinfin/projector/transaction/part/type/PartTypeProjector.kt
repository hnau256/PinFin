package hnau.pinfin.projector.transaction.part.type

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed interface PartTypeProjector {

    @Composable
    fun MainContent(
        modifier: Modifier,
    )

    @Composable
    fun AmountContent(
        modifier: Modifier,
    )
}