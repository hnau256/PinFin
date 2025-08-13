package hnau.pinfin.projector.transaction.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.collectAsTextFieldValueMutableAccessor
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.transaction.page.CommentPageModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.Label
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
                .horizontalDisplayPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
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

            model
                .suggests
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { suggests ->
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        suggests
                            .value
                            .forEach { suggest ->
                                Label(
                                    onClick = suggest.onClick,
                                ) {
                                    Text(
                                        text = suggest.comment.text,
                                    )
                                }
                            }
                    }
                }
        }
    }
}