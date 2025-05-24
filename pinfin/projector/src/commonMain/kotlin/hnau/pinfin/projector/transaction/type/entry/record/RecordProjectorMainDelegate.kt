package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.TextInput
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.table.cellShape
import hnau.common.projector.utils.Icon
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.comment
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pipe.annotations.Pipe
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

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
        ) {
            Subtable {
                Cell {
                    TextInput(
                        modifier = Modifier.weight(1f),
                        value = model.comment,
                        shape = cellShape,
                        placeholder = { Text(stringResource(Res.string.comment)) },
                    )
                }
                model
                    .openRemoveOverlap
                    .collectAsState()
                    .value
                    ?.let { openRemoveOverlap ->
                        Cell {
                            HnauButton(
                                shape = cellShape,
                                content = { Icon(Icons.Filled.Clear) },
                                onClick = openRemoveOverlap,
                            )
                        }
                    }
            }
            Subtable {
                Cell {
                    CategoryButton(
                        modifier = Modifier.weight(1f),
                        shape = cellShape,
                        info = model.category.collectAsState().value,
                        onClick = model::openCategoryChooser,
                    )
                }
                amount.Content(
                    scope = this,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}