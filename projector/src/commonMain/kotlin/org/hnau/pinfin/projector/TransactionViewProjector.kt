package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.DialogContentInfo
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SDialog
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.utils.Importance
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.pinfin.model.TransactionViewModel

class TransactionViewProjector(
    scope: CoroutineScope,
    private val model: TransactionViewModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    private val removeDialog = model
        .removeDialog
        .mapState(scope) { removeOrNull ->
            removeOrNull?.let { remove ->
                DialogContentInfo(
                    content = {
                        SText(dependencies.localization.removeTransaction)
                    },
                    actions = {
                        FContext(
                            update = {
                                copy(
                                    mood = Mood.Error,
                                )
                            }
                        ) {
                            Action(
                                actionOrElseOrDisabled = remove.remove.collectAsState().value,
                                titleOrIcon = TitleOrIcon.Both(
                                    title = dependencies.localization.yes,
                                    icon = Drawable.Vector(Icons.Default.Delete)
                                )
                            )
                        }
                        Action(
                            importanceToActivate = Importance.Tertiary,
                            actionOrElseOrDisabled = ActionOrElse.instant(remove.cancel),
                            titleOrIcon = TitleOrIcon.Both(
                                title = dependencies.localization.cancel,
                                icon = Drawable.Vector(Icons.Default.Cancel),
                            ),
                        )
                    },
                    cancel = remove.cancel,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText(dependencies.localization.transaction) },
            actions = {
                FContext(
                    update = {
                        copy(
                            mood = Mood.Error,
                        )
                    }
                ) {
                    Action(
                        actionOrElseOrDisabled = ActionOrElse.instant { model.remove() },
                        titleOrIcon = TitleOrIcon.Icon(Drawable.Vector(Icons.Default.Delete))
                    )
                }
            },
        ) {
            SContentWithActions(
                content = {},
                actions = {
                    SButton(
                        actionOrElseOrDisabled = ActionOrElse.instant { model.edit() },
                        titleOrIcon = TitleOrIcon.Both(
                            title = dependencies.localization.edit,
                            icon = Drawable.Vector(Icons.Default.Edit),
                        )
                    )
                },
            )
            SDialog(
                info = removeDialog,
            )
        }
    }
}