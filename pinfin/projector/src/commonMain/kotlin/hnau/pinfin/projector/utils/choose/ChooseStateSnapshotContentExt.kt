package hnau.pinfin.projector.utils.choose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.model.EditingString
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.row.ChipsFlowRow
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.table.TableScope
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.utils.choose.ChooseStateSnapshot
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.search_create
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> ChooseStateSnapshot<T>.Content(
    query: MutableStateFlow<EditingString>,
    onReady: () -> Unit,
    updateSelected: (T) -> Unit,
    messages: ChooseMessages,
    itemContent: @Composable (value: T, selected: Boolean, onClick: () -> Unit) -> Unit,
) {
    Table(
        orientation = TableOrientation.Vertical,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val variantsToAdd: (@Composable TableScope.() -> Unit)? = possibleVariantsToAdd
            ?.let { possibleVariantsToAddNotEmpty ->
                {
                    CellBox(
                        contentAlignment = Alignment.TopStart,
                        isLast = true,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                            modifier = Modifier.padding(
                                horizontal = Dimens.separation,
                                vertical = Dimens.smallSeparation,
                            ),
                        ) {
                            Text(
                                text = messages.createNew,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            ChipsFlowRow(
                                all = possibleVariantsToAddNotEmpty,
                            ) { item ->
                                itemContent(
                                    item,
                                    false,
                                    {
                                        updateSelected(item)
                                        onReady()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        val suggestsAreLast = variantsToAdd == null
        val suggests: (@Composable TableScope.() -> Unit)? =
            when (val visibleVariants = visibleVariants) {
                ChooseStateSnapshot.VisibleVariants.InputToCreateNewMessage -> {
                    {
                        MessageCell(
                            isLast = suggestsAreLast,
                            message = messages.noVariants,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                ChooseStateSnapshot.VisibleVariants.Empty -> null

                ChooseStateSnapshot.VisibleVariants.NotFound -> {
                    {
                        MessageCell(
                            isLast = suggestsAreLast,
                            message = messages.notFound,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is ChooseStateSnapshot.VisibleVariants.List -> {
                    {
                        CellBox(
                            isLast = suggestsAreLast,
                            contentAlignment = Alignment.TopStart,
                        ) {
                            ChipsFlowRow(
                                all = visibleVariants.list,
                                modifier = Modifier.padding(Dimens.smallSeparation),
                            ) { (item, selected) ->
                                itemContent(
                                    item,
                                    selected,
                                    {
                                        updateSelected(item)
                                        onReady()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        Subtable(
            isLast = suggests == null && variantsToAdd == null,
        ) {
            Cell(
                isLast = false,
            ) { modifier ->
                HnauButton(
                    content = { Icon(Icons.Filled.ArrowBack) },
                    shape = shape,
                    onClick = onReady,
                    modifier = modifier,
                )
            }
            Cell(
                isLast = true,
            ) { modifier ->
                val focusRequester = remember { FocusRequester() }
                TextInput(
                    modifier = modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .fillMaxHeight(),
                    value = query,
                    shape = shape,
                    maxLines = 1,
                    placeholder = { Text(stringResource(Res.string.search_create)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                )
                val requestFocus = when (visibleVariants) {
                    ChooseStateSnapshot.VisibleVariants.Empty,
                    ChooseStateSnapshot.VisibleVariants.InputToCreateNewMessage,
                        -> true

                    is ChooseStateSnapshot.VisibleVariants.List,
                    ChooseStateSnapshot.VisibleVariants.NotFound,
                        -> false
                }
                LaunchedEffect(focusRequester, requestFocus) {
                    if (requestFocus) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
        suggests?.invoke(this)
        variantsToAdd?.invoke(this)
    }
}

@Composable
private fun TableScope.MessageCell(
    isLast: Boolean,
    message: String,
    color: Color,
) {
    CellBox(
        isLast = isLast,
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(
                Dimens.separation,
                Dimens.smallSeparation,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
    }
}