package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.kotlin.foldBoolean

@Composable
fun SelectButton(
    content: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    shape: Shape = HnauShape(),
    onClick: (() -> Unit)?,
) {
    selected.foldBoolean(
        ifTrue = {
            Button(
                modifier = modifier,
                onClick = { onClick?.invoke() },
                enabled = onClick != null,
                shape = shape,
            ) {
                content()
            }
        },
        ifFalse = {
            OutlinedButton(
                modifier = modifier,
                onClick = { onClick?.invoke() },
                enabled = onClick != null,
                shape = shape,
            ) {
                content()
            }
        }
    )
}