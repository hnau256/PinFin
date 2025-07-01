package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.kotlin.coroutines.mapState
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.TextInput
import hnau.common.projector.uikit.table.Cell
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.utils.Icon
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

    private val cells: ImmutableList<Cell> = persistentListOf(
        Subtable(
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
        ),
        Subtable(
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
                )
            )
        )
    )

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            cells = cells,
        )
    }
}