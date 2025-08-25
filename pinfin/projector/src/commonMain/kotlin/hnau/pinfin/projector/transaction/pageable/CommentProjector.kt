package hnau.pinfin.projector.transaction.pageable

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.collectAsTextFieldValueMutableAccessor
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction.pageable.CommentModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.transaction_old_2.part.PartDefaults
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

    class Page(
        scope: CoroutineScope,
        private val model: CommentModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier = Modifier,
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


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
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