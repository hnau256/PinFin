package org.hnau.pinfin.projector.budget.manage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.DialogContentInfo
import org.hnau.commons.app.projector.fractal.SDialog
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.pinfin.model.budget.manage.BudgetManageRemoveModel
import org.hnau.pinfin.projector.Localization

class BudgetManageRemoveProjector(
    scope: CoroutineScope,
    private val model: BudgetManageRemoveModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    fun onRemoveClick() {
        model.onRemoveClick()
    }

    private val dialog: StateFlow<DialogContentInfo?> = model
        .dialog
        .mapState(scope) { dialogOrNull ->
            dialogOrNull?.let { dialog ->
                DialogContentInfo(
                    content = {
                        SCell {
                            SPanel {
                                SText(
                                    text = dependencies.localization.removeBudget,
                                    type = SizeType.Large,
                                )
                            }
                        }
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
                                modifier = Modifier.weight(1f),
                                actionOrElseOrDisabled = dialog.remove.collectAsState().value,
                                titleOrIcon = TitleOrIcon.Title(dependencies.localization.yes),
                            )
                        }
                        Action(
                            modifier = Modifier.weight(1f),
                            actionOrElseOrDisabled = ActionOrElse.instant(dialog.cancel),
                            titleOrIcon = TitleOrIcon.Title(dependencies.localization.no),
                        )
                    },
                    cancel = dialog.cancel,
                )
            }
        }

    @Composable
    fun Content() {
        SDialog(
            info = dialog,
        )
    }
}