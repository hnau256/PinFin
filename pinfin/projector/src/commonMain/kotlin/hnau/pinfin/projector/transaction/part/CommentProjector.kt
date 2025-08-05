package hnau.pinfin.projector.transaction.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.part.CommentModel
import hnau.pinfin.projector.utils.Label
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class CommentProjector(
    scope: CoroutineScope,
    private val model: CommentModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            ItemsRow {
                Icon(Icons.Filled.Storefront)
                Text(
                    modifier = Modifier.weight(1f),
                    text = model
                        .comment
                        .collectAsState()
                        .value
                        .text,
                )
            }
        }
    }
}