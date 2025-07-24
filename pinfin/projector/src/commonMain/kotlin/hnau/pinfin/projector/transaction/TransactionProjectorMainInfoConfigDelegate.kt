package hnau.pinfin.projector.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import hnau.common.app.model.toEditingString
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TripleRow
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.SuggestsListProjector
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pinfin.projector.utils.title
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource


class TransactionProjectorMainInfoConfigDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Config,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    private val commentIsFocused: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    private val commentSuggests: SuggestsListProjector = SuggestsListProjector(
        scope = scope,
        suggests = model.commentSuggests,
        onSelect = { selectedSuggest ->
            model.comment.value = selectedSuggest.text.toEditingString()
        },
        inputIsFocused = commentIsFocused,
    )

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Subtable(
                isLast = false,
            ) {
                Cell(
                    isLast = false,
                ) { modifier ->
                    HnauButton(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseDate,
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.CalendarMonth) },
                                content = {
                                    Text(
                                        text = model
                                            .date
                                            .collectAsState()
                                            .value
                                            .let(dependencies.dateTimeFormatter::formatDate),
                                    )
                                }
                            )
                        }
                    )
                }
                Cell(
                    isLast = true,
                ) { modifier ->
                    HnauButton(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseTime,
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.Schedule) },
                                content = {
                                    Text(
                                        text = model
                                            .time
                                            .collectAsState()
                                            .value
                                            .let(dependencies.dateTimeFormatter::formatTime),
                                    )
                                }
                            )
                        }
                    )
                }
            }
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
                    val focusRequester = remember { FocusRequester() }
                    TextInput(
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { state ->
                                commentIsFocused.value = state.isFocused
                            },
                        placeholder = { Text(stringResource(Res.string.comment)) },
                        value = model.comment,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        )
                    )
                    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
                    commentSuggests.Content(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Subtable(
                isLast = true,
            ) {
                TransactionType
                    .entries
                    .mapIndexed { i, type ->
                        Cell(
                            isLast = i == TransactionType.entries.lastIndex,
                        ) { modifier ->
                            val selectedType by model.typeVariant.collectAsState()
                            HnauButton(
                                modifier = modifier.weight(1f),
                                shape = shape,
                                onClick = { model.chooseType(type) },
                                style = when (type) {
                                    selectedType -> ContainerStyle.Primary
                                    else -> ContainerStyle.Neutral
                                },
                                content = {
                                    TripleRow(
                                        leading = when (type) {
                                            selectedType -> {
                                                {
                                                    Icon(Icons.Filled.Done)
                                                }
                                            }

                                            else -> null
                                        },
                                        content = {
                                            Text(
                                                text = type.title,
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
            }
        }
    }

}