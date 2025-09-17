package hnau.pinfin.projector.transaction.delegates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.TransactionModel

class TopBarActionsProjector(
    private val model: TransactionModel,
) {


    @Composable
    fun Content() {
        model
            .remove
            ?.let { remove ->
                IconButton(
                    onClick = remove,
                ) {
                    Icon(Icons.Default.Delete)
                }
            }
        val saveOrNull = model
            .saveOrDisabled
            .collectAsState()
            .value
        IconButton(
            onClick = { saveOrNull?.invoke() },
            enabled = saveOrNull != null,
        ) {
            Icon(Icons.Default.Save)
        }
    }
}