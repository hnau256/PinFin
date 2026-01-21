package hnau.pinfin.projector.transaction.delegates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.app.projector.uikit.TopBarAction
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
                TopBarAction(
                    onClick = remove,
                ) {
                    Icon(Icons.Default.Delete)
                }
            }
        val saveOrNull = model
            .saveOrDisabled
            .collectAsState()
            .value
        TopBarAction(
            onClick = saveOrNull,
        ) {
            Icon(Icons.Default.Save)
        }
    }
}