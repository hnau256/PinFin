package hnau.pinfin.projector.transaction.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction_old_2.part.CommentModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.Label
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

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
                val text = model
                    .comment
                    .collectAsState()
                    .value
                    .text
                    .takeIf(String::isNotEmpty)
                Text(
                    modifier = Modifier.weight(1f),
                    text = text ?: stringResource(Res.string.comment),
                    color = text.foldNullable(
                        ifNull = { LocalContentColor.current.copy(alpha = 0.5f) },
                        ifNotNull = { Color.Unspecified },
                    ),
                    fontStyle = text.foldNullable(
                        ifNull = { FontStyle.Italic },
                        ifNotNull = { null },
                    )
                )
            }
        }
    }
}