package org.hnau.pinfin.projector.transaction.delegates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.hnau.commons.app.projector.fractal.STableActionsScope
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.pinfin.model.transaction.TransactionModel

class TopBarActionsProjector(
    private val model: TransactionModel,
) {


    @Composable
    fun STableActionsScope.Content() {
        model
            .remove
            ?.let { remove ->
                Action(
                    actionOrElseOrDisabled = ActionOrElse.instant(remove),
                    titleOrIcon = TitleOrIcon.Icon(Drawable.Vector(Icons.Default.Delete))
                )
            }
        Action(
            actionOrElseOrDisabled = model
                .saveOrDisabled
                .collectAsState()
                .value,
            titleOrIcon = TitleOrIcon.Icon(Drawable.Vector(Icons.Default.Delete))
        )
    }
}