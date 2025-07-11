package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import arrow.core.identity
import arrow.core.toOption
import hnau.common.kotlin.coroutines.Stickable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.predeterminated
import hnau.common.kotlin.coroutines.stateFlow
import hnau.common.kotlin.coroutines.stick
import hnau.common.kotlin.foldNullable
import hnau.common.model.toEditingString
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.TextInput
import hnau.common.projector.uikit.shape.HnauShape
import hnau.common.projector.uikit.table.Cell
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.projector.utils.Icon
import hnau.common.projector.utils.toTextFieldValue
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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

    private val cells: StateFlow<ImmutableList<Cell>> = run {
        val header: Cell = Subtable(
            cells = model
                .openRemoveOverlap
                .mapState(scope) { removeOrNull ->
                    listOfNotNull(
                        Cell {
                            TextInput(
                                modifier = Modifier.weight(1f),
                                value = model.comment,
                                shape = shape,
                                placeholder = { Text(stringResource(Res.string.comment)) },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                ),
                            )
                        },
                        removeOrNull?.let { remove ->
                            Cell {
                                HnauButton(
                                    shape = shape,
                                    content = { Icon(Icons.Filled.Clear) },
                                    onClick = remove,
                                )
                            }
                        }
                    ).toImmutableList()
                }
        )
        val footer: Cell = Subtable(
            cells = persistentListOf(
                Cell {
                    CategoryButton(
                        modifier = Modifier.weight(1f),
                        shape = shape,
                        info = model.category.collectAsState().value,
                        onClick = model::openCategoryChooser,
                    )
                },
                amount.Content(
                    weight = 1f,
                    imeAction = ImeAction.Next,
                    onImeAction = model.createNextIfLast.mapState(scope) { createNextIfLastOrNull ->
                        createNextIfLastOrNull?.let { createNextIfLast ->
                            {
                                createNextIfLast()
                                scope.launch {
                                    delay(100)
                                    defaultKeyboardAction(ImeAction.Next)
                                }
                            }
                        }
                    },
                )
            )
        )
        model
            .commentSuggests
            .stick(scope) { stickScope, suggestsOrNull ->
                suggestsOrNull.foldNullable(
                    ifNull = {
                        Stickable.predeterminated(null)
                    },
                    ifNotNull = { suggests ->
                        Stickable.stateFlow(
                            initial = suggests,
                            tryUseNext = List<Comment>?::toOption,
                            createResult = ::identity,
                        )
                    }
                )
            }
            .mapState(scope) { suggestsOrNull ->
                listOfNotNull(
                    header,
                    suggestsOrNull?.let { suggestsStateFlow ->
                        Cell {
                            val suggests by suggestsStateFlow.collectAsState()
                            LazyRow(
                                modifier = Modifier
                                    .width(0.dp) //fake
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = shape
                                    ),
                                contentPadding = PaddingValues(horizontal = Dimens.smallSeparation),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                            ) {
                                items(
                                    items = suggests,
                                    key = Comment::text,
                                ) { comment ->
                                    SuggestionChip(
                                        onClick = {
                                            model.comment.value = comment.text.toEditingString()
                                        },
                                        label = { Text(comment.text) },
                                        shape = HnauShape(),
                                    )
                                }
                            }
                        }
                    },
                    footer,
                ).toImmutableList()
            }
    }

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            cells = cells.collectAsState().value,
        )
    }
}