package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.table.TableScope
import hnau.common.app.projector.uikit.utils.Dimens

@Composable
fun Dialog(
    title: String,
    buttons: @Composable TableScope.() -> Unit,
) {
    Table(
        orientation = TableOrientation.Vertical,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CellBox(
            isLast = false,
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimens.separation),
            )
        }
        Subtable(
            isLast = true,
        ) {
            buttons()
        }
    }
}