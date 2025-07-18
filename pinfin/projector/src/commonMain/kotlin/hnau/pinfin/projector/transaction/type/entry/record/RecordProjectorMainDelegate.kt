package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import hnau.common.app.model.toEditingString
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.table.TableScope
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class RecordProjectorMainDelegate(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val model: RecordModel,
) {

    @Pipe
    interface Dependencies {

        fun amount(): AmountProjector.Dependencies
    }

    private val amount = AmountProjector(
        scope = scope,
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    private val commentIsFocused: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val suggests = commentIsFocused.flatMapState(scope) { focused ->
        focused.foldBoolean(
            ifTrue = { model.commentSuggests },
            ifFalse = { null.toMutableStateFlowAsInitial() },
        )
    }

    private val amountImeAction: StateFlow<(KeyboardActionScope.() -> Unit)?> =
        model.createNextIfLast.mapState(scope) { createNextIfLastOrNull ->
            createNextIfLastOrNull?.let { createNextIfLast ->
                {
                    createNextIfLast()
                    scope.launch {
                        delay(100)
                        defaultKeyboardAction(ImeAction.Next)
                    }
                }
            }
        }

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {

            Cell(
                isLast = false,
            ) { modifier ->
                Column(
                    modifier = modifier
                        .background(
                            shape = shape,
                            color = TableDefaults.cellColor,
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                    ) {
                        TextInput(
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    commentIsFocused.value = focusState.isFocused
                                },
                            value = model.comment,
                            placeholder = { Text(stringResource(Res.string.comment)) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                            ),
                        )
                        model
                            .openRemoveOverlap
                            .collectAsState()
                            .value
                            .NullableStateContent(
                                modifier = Modifier.fillMaxHeight(),
                                transitionSpec = TransitionSpec.horizontal(),
                            ) { remove ->
                                IconButton(
                                    onClick = remove,
                                ) {
                                    Icon(Icons.Filled.Delete)
                                }
                            }
                    }
                    suggests
                        .collectAsState()
                        .value
                        .NullableStateContent(
                            modifier = Modifier.fillMaxWidth(),
                            transitionSpec = TransitionSpec.vertical(),
                            label = "CommentSuggests"
                        ) { suggestsFlow ->
                            val suggests by suggestsFlow.collectAsState()
                            LazyRow(
                                modifier = Modifier
                                    .width(128.dp)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = Dimens.smallSeparation),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = suggests,
                                    key = Comment::text,
                                ) { comment ->
                                    Text(
                                        text = comment.text,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .clip(HnauShape())
                                            .clickable {
                                                model.comment.value =
                                                    comment.text.toEditingString()
                                            }
                                            .padding(
                                                vertical = Dimens.smallSeparation,
                                                horizontal = Dimens.separation,
                                            )
                                    )
                                }
                            }
                        }
                }
            }
            Subtable(
                isLast = true,
            ) {
                Cell(
                    isLast = false,
                ) { modifier ->
                    CategoryButton(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        info = model.category.collectAsState().value,
                        onClick = model::openCategoryChooser,
                    )
                }
                Cell(
                    isLast = true,
                ) { modifier ->
                    amount.Content(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        imeAction = ImeAction.Next,
                        onImeAction = amountImeAction,
                    )
                }
            }
        }
    }
}