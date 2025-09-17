package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.collectAsTextFieldValueMutableAccessor
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.kotlin.foldBoolean
import hnau.pinfin.model.transaction.pageable.CommentModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.transaction.utils.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.LabelDefaults
import org.jetbrains.compose.resources.stringResource

class CommentProjector(
    private val model: CommentModel,
) {

    class Page(
        private val model: CommentModel.Page,
    ) {

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            model
                .suggests
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = modifier
                        .padding(contentPadding)
                        .fillMaxWidth()
                        .horizontalDisplayPadding()
                        .imePadding(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { suggests ->
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
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


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {

        var value by model.commentEditingString.collectAsTextFieldValueMutableAccessor()
        val focusRequester = remember { FocusRequester() }
        val isFocused = model.isFocused
        OutlinedTextField(
            colors = PartDefaults.outlinedTextFieldColors,
            maxLines = 3,
            shape = LabelDefaults.shape,
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        model.requestFocus()
                    }
                },
            value = value,
            onValueChange = { newValue ->
                if (isFocused.value) { //this check blocks send old value on unfocused
                    value = newValue
                }
            },
            placeholder = { Text(stringResource(Res.string.comment)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions { model.goForward() },
        )
        val focusManager = LocalFocusManager.current
        LaunchedEffect(isFocused) {
            isFocused.collect { focused ->
                focused.foldBoolean(
                    ifTrue = { focusRequester.requestFocus() },
                    ifFalse = { focusManager.clearFocus() },
                )
            }
        }
    }
}