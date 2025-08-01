package hnau.pinfin.projector.transaction_old

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction_old.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.SuggestsListProjector
import hnau.pinfin.projector.utils.colors
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
                    Button(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseDate,
                        content = {
                            Icon(Icons.Filled.CalendarMonth)
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
                Cell(
                    isLast = true,
                ) { modifier ->
                    Button(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseTime,
                        content = {
                            Icon(Icons.Filled.Schedule)
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
                            Button(
                                modifier = modifier.weight(1f),
                                shape = shape,
                                onClick = { model.chooseType(type) },
                                colors = ButtonDefaults.colors(
                                    container = when (type) {
                                        selectedType -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceBright
                                    },
                                ),
                                content = {
                                    if (type == selectedType) {
                                        Icon(Icons.Filled.Done)
                                    }
                                    Text(
                                        text = type.title,
                                    )
                                }
                            )
                        }
                    }
            }
        }
    }

}