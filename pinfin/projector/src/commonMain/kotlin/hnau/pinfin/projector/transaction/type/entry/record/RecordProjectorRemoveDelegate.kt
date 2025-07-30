package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.remove_record
import hnau.pinfin.projector.resources.yes
import hnau.pinfin.projector.utils.Dialog
import hnau.pinfin.projector.utils.colors
import org.jetbrains.compose.resources.stringResource

class RecordProjectorRemoveDelegate(
    private val remove: () -> Unit,
    private val model: RecordModel,
) {

    @Composable
    fun Content() {
        Dialog(
            title = stringResource(Res.string.remove_record),
        ) {
            Cell(
                isLast = false,
            ) {modifier ->
                Button(
                    shape = shape,
                    modifier = modifier.weight(1f),
                    onClick = model::closeOverlap,
                ) {
                    Text(
                        text = stringResource(Res.string.no),
                    )
                }
            }
            Cell(
                isLast = true,
            ) {modifier ->
                Button(
                    shape = shape,
                    modifier = modifier.weight(1f),
                    onClick = remove,
                    colors = ButtonDefaults.colors(
                        container = MaterialTheme.colorScheme.surfaceBright,
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.yes),
                    )
                }
            }
        }
    }
}