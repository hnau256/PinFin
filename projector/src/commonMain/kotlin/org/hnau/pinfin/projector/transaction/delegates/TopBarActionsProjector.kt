package org.hnau.pinfin.projector.transaction.delegates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.hnau.commons.app.projector.fractal.STableActionsScope
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.pinfin.model.transaction.TransactionModel

class TopBarActionsProjector(
    private val model: TransactionModel,
) {


    @Composable
    context(scope: STableActionsScope)
    fun Content() {
        scope.Action(
            actionOrElseOrDisabled = model
                .saveOrDisabled
                .collectAsState()
                .value,
            titleOrIcon = TitleOrIcon.Icon(Drawable.Vector(Icons.Default.Save))
        )
    }
}