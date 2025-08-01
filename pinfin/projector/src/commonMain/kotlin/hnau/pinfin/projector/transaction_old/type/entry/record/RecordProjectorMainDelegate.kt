package hnau.pinfin.projector.transaction_old.type.entry.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import hnau.common.app.model.toEditingString
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.toEnum
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.transaction_old.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.SuggestsListProjector
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.CategoryButton
import hnau.pinfin.projector.utils.icon
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
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

    private val suggestsList: SuggestsListProjector = SuggestsListProjector(
        scope = scope,
        inputIsFocused = commentIsFocused,
        suggests = model.commentSuggests,
        onSelect = { selectedComment ->
            model.comment.value = selectedComment.text.toEditingString()
        },
    )

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

    private val bringIntoViewRequester = BringIntoViewRequester()

    private val hasFocus: MutableStateFlow<Boolean> =
        false.toMutableStateFlowAsInitial()

    private val size: MutableStateFlow<IntSize> =
        IntSize.Zero.toMutableStateFlowAsInitial()

    init {
        scope.launch {
            hasFocus
                .flatMapState(scope) { hasFocus ->
                    hasFocus.foldBoolean(
                        ifFalse = { null.toMutableStateFlowAsInitial() },
                        ifTrue = { size }
                    )
                }
                .filterNotNull()
                .collectLatest {
                    bringIntoViewRequester.bringIntoView()
                }
        }
    }

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { hasFocus.value = it.hasFocus }
                .onSizeChanged {newSize -> size.value = newSize },
        ) {
            Cell(
                isLast = false,
            ) { modifier ->
                Column(
                    modifier = modifier
                        .width(0.dp)
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
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    commentIsFocused.value = focusState.isFocused
                                },
                            value = model.comment,
                            placeholder = { Text(stringResource(Res.string.comment)) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            )
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
                    suggestsList.Content(
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                        info = model
                            .category
                            .collectAsState()
                            .value,
                        onClick = model::openCategoryChooser,
                    )
                }
                Cell(
                    isLast = false,
                ) { modifier ->
                    val direction by model.direction.collectAsState()
                    SwitchHueToAmountDirection(
                        amountDirection = direction,
                    ) {
                        Button(
                            modifier = modifier,
                            shape = shape,
                            onClick = { model.switchDirection() },
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Icon(
                                icon = direction.icon,
                            )
                        }
                    }
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

    companion object {

        private val booleanDirectionMapper: Mapper<Boolean, AmountDirection> =
            Mapper.toEnum<Boolean, AmountDirection>(
                extractValue = {
                    when (this) {
                        AmountDirection.Credit -> true
                        AmountDirection.Debit -> false
                    }
                }
            )
    }
}