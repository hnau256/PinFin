package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import hnau.common.app.model.toEditingString
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.toEnum
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.SuggestsListProjector
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
                        info = model.category.collectAsState().value,
                        onClick = model::openCategoryChooser,
                    )
                }
                Cell(
                    isLast = false,
                ) { modifier ->
                    HnauButton(
                        modifier = modifier,
                        shape = shape,
                        onClick = { model.switchDirection() },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                            ) {
                                val color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                                CompositionLocalProvider(
                                    LocalContentColor provides color
                                ) {
                                    Icon(Icons.Default.ArrowDropUp)
                                    Icon(Icons.Default.ArrowDropDown)
                                }
                            }
                            model
                                .direction
                                .collectAsState()
                                .value
                                .StateContent(
                                    label = "AmountDirection",
                                    transitionSpec = TransitionSpec.vertical(),
                                    contentKey = { it },
                                ) { direction ->
                                    Text(
                                        text = when (direction) {
                                            AmountDirection.Credit -> "+"
                                            AmountDirection.Debit -> "-"
                                        },
                                        color = when (direction) {
                                            AmountDirection.Credit -> MaterialTheme.colorScheme.primary
                                            AmountDirection.Debit -> MaterialTheme.colorScheme.error
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
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