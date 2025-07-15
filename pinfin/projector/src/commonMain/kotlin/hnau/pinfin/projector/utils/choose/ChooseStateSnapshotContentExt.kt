package hnau.pinfin.projector.utils.choose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import hnau.common.app.model.EditingString
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.row.ChipsFlowRow
import hnau.common.app.projector.uikit.table.Cell
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.utils.choose.ChooseStateSnapshot
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.search_create
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
        cells = listOfNotNull(
            Subtable(
                cells = persistentListOf(
                    Cell {
                        HnauButton(
                            content = { Icon(Icons.Filled.ArrowBack) },
                            shape = shape,
                            onClick = onReady,
                        )
                    },
                    Cell {
                        val focusRequester = remember { FocusRequester() }
                        TextInput(
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f),
                            value = query,
                            shape = shape,
                            placeholder = { Text(stringResource(Res.string.search_create)) },
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
                )
            ),
            when (val visibleVariants = visibleVariants) {
                ChooseStateSnapshot.VisibleVariants.InputToCreateNewMessage -> MessageCell(
                    message = messages.noVariants,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                ChooseStateSnapshot.VisibleVariants.Empty -> null

                ChooseStateSnapshot.VisibleVariants.NotFound -> MessageCell(
                    message = messages.notFound,
                    color = MaterialTheme.colorScheme.error,
                )

                is ChooseStateSnapshot.VisibleVariants.List -> CellBox(
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
            },
            possibleVariantsToAdd?.let { possibleVariantsToAddNotEmpty ->
                CellBox(
                    contentAlignment = Alignment.TopStart,
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
        ).toImmutableList(),
    )
}

private fun MessageCell(
    message: String,
    color: Color,
): Cell = CellBox(
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