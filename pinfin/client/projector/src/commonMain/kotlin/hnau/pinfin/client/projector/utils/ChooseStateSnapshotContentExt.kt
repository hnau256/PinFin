package hnau.pinfin.client.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.EditingString
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.row.ChipsFlowRow
import hnau.common.compose.uikit.table.CellBox
import hnau.common.compose.uikit.table.Subtable
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.table.cellShape
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import kotlinx.coroutines.flow.MutableStateFlow
import hnau.pinfin.client.model.utils.choose.ChooseStateSnapshot

@Composable
fun <T> ChooseStateSnapshot<T>.Content(
    query: MutableStateFlow<EditingString>,
    onReady: () -> Unit,
    updateSelected: (T) -> Unit,
    itemContent: @Composable (value: T, selected: Boolean, onClick: () -> Unit) -> Unit,
) {
    Table(
        orientation = TableOrientation.Vertical,
    ) {
        Subtable {
            Cell {
                HnauButton(
                    content = { Icon { Icons.Filled.ArrowBack } },
                    shape = cellShape,
                    onClick = onReady,
                )
            }
            Cell {
                TextInput(
                    modifier = Modifier.Companion
                        .weight(1f),
                    value = query,
                    shape = cellShape,
                    //TODO("ComposeForAndroid")
                    placeholder = { Text("QWERTY"/*stringResource(R.string.search_create)*/) },
                )
            }
        }
        visibleVariants
            .toNonEmptyListOrNull()
            ?.let { visibleVariantsNotEmpty ->
                CellBox(
                    contentAlignment = Alignment.TopStart,
                ) {
                    ChipsFlowRow(
                        all = visibleVariantsNotEmpty,
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
        possibleVariantsToAdd
            .toNonEmptyListOrNull()
            ?.let { possibleVariantsToAddNotEmpty ->
                CellBox(
                    contentAlignment = Alignment.TopStart,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                        modifier = Modifier.padding(Dimens.smallSeparation),
                    ) {
                        Text(
                            //TODO("ComposeForAndroid")
                            text = "QWERTY",//stringResource(R.string.create),
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
}