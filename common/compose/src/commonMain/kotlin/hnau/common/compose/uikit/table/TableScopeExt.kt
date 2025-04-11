package hnau.common.compose.uikit.table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun TableScope.Subtable(
    modifier: Modifier = Modifier,
    content: @Composable TableScope.() -> Unit,
) {
    Cell {
        Table(
            modifier = modifier,
            orientation = orientation.opposite,
            content = content,
            parentCellScope = this,
        )
    }
}

@Composable
fun TableScope.CellBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.(Shape) -> Unit,
) {
    Cell {
        Box(
            modifier = modifier.background(
                color = backgroundColor,
                shape = cellShape
            ),
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints,
        ) {
            content(cellShape)
        }
    }
}

