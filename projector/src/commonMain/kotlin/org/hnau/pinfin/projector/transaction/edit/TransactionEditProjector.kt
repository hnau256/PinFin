package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.DialogContentInfo
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SDialog
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.transaction.edit.TransactionEditModel
import org.hnau.pinfin.projector.Localization

class TransactionEditProjector(
    scope: CoroutineScope,
    private val model: TransactionEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun date(): DateEditProjector.Dependencies

        fun comment(): CommentEditProjector.Dependencies

        fun type(): TransactionTypeEditProjector.Dependencies
    }

    private val date = DateEditProjector(
        model = model.date,
        dependencies = dependencies.date(),
    )

    private val comment = CommentEditProjector(
        model = model.comment,
        dependencies = dependencies.comment(),
    )

    private val type = TransactionTypeEditProjector(
        scope = scope,
        model = model.type,
        currency = model.currency,
        dependencies = dependencies.type(),
    )

    private val dialogInfo = model
        .goBackDelegate
        .dialog
        .mapState(scope) { dialog ->
            dialog?.let { dialogNotNull ->
                DialogContentInfo(
                    content = {
                        SText(dependencies.localization.saveChanges)
                    },
                    actions = {
                        Action(
                            actionOrElseOrDisabled = ActionOrElse.instant(dialogNotNull.exitWithoutSaving),
                            titleOrIcon = TitleOrIcon.Title(dependencies.localization.notSave),
                        )
                        dialogNotNull
                            .saveAndExitIfPossible
                            .foldNullable(
                                ifNull = {
                                    Action(
                                        actionOrElseOrDisabled = ActionOrElse.instant(dialogNotNull.returnToEditing),
                                        titleOrIcon = TitleOrIcon.Title(dependencies.localization.close),
                                    )
                                },
                                ifNotNull = { saveAction ->
                                    Action(
                                        actionOrElseOrDisabled = saveAction.collectAsState().value,
                                        titleOrIcon = TitleOrIcon.Title(dependencies.localization.save),
                                    )
                                },
                            )
                    },
                    cancel = dialogNotNull.returnToEditing,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val localization = dependencies.localization
        val typeValue by type.value.collectAsState()
        val recordProjectors = typeValue.fold(
            ifEntry = { entry -> entry.records.records.collectAsState().value },
            ifTransfer = { null },
        )

        SScreen(
            contentPadding = contentPadding,
            title = {
                SText(localization.transaction)
            },
        ) {
            SContentWithActions(
                content = {
                    SLazyTable(
                        orientation = Orientation.Vertical,
                    ) {
                        cell(key = "date") {
                            date.Content()
                        }
                        cell(key = "comment") {
                            comment.Content()
                        }
                        cell(key = "type") {
                            type.Content()
                        }
                        typeValue.fold(
                            ifEntry = { entry ->
                                recordProjectors?.let { recordProjectorsNotNull ->
                                    entry.Content(
                                        scope = this,
                                        recordProjectors = recordProjectorsNotNull,
                                    )
                                }
                            },
                            ifTransfer = { transfer ->
                                transfer.Content(
                                    scope = this,
                                )
                            },
                        )
                    }
                },
                actions = {
                    val saveAction by model
                        .goBackDelegate
                        .saveOrInactive
                        .collectAsState()
                    val saveActionValue = saveAction
                        ?.collectAsState()
                        ?.value
                    SButton(
                        actionOrElseOrDisabled = saveActionValue,
                        titleOrIcon = TitleOrIcon.Both(
                            title = localization.save,
                            icon = Drawable.Vector(Icons.Default.Save),
                        ),
                    )
                },
            )
            SDialog(
                info = dialogInfo,
            )
        }
    }
}
