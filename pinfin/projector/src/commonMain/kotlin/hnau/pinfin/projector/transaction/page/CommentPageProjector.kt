package hnau.pinfin.projector.transaction.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.utils.collectAsTextFieldValueMutableAccessor
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.transaction.part.page.CommentPageModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class CommentPageProjector(
    scope: CoroutineScope,
    private val model: CommentPageModel,
    dependencies: Dependencies,
) : PageProjector {

    @Pipe
    interface Dependencies

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        modifier: Modifier,
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = modifier
                .padding(contentPadding)
                .horizontalDisplayPadding(),
        ) {
            var value by model.comment.collectAsTextFieldValueMutableAccessor()
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = value,
                onValueChange = { newValue -> value = newValue },
                maxLines = 1,
                shape = MaterialTheme.shapes.medium,
                label = { Text(stringResource(Res.string.comment)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    //imeAction =
                ),
                keyboardActions = KeyboardActions {
                    TODO()
                }
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
}