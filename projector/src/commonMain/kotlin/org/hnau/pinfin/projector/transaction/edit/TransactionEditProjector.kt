package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.separator
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.TransactionEditModel
import org.hnau.pinfin.model.transaction.edit.fold
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter

class TransactionEditProjector(
    private val model: TransactionEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val amountFormatter: AmountFormatter

        val dateTimeFormatter: DateTimeFormatter

        val budgetRepository: BudgetRepository
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val localization = dependencies.localization
        val amountFormatter = dependencies.amountFormatter
        val dateTimeFormatter = dependencies.dateTimeFormatter
        val budgetRepository = dependencies.budgetRepository

        val type = model.type.type.collectAsState().value
        val records = type.fold(
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
                            val projector = remember(model) {
                                DateEditProjector(
                                    model = model.date,
                                    localization = localization,
                                    dateTimeFormatter = dateTimeFormatter,
                                )
                            }
                            projector.Content()
                        }
                        cell(key = "comment") {
                            val projector = remember(model) {
                                CommentEditProjector(
                                    model = model.comment,
                                    localization = localization,
                                )
                            }
                            projector.Content()
                        }
                        cell(key = "type") {
                            val projector = remember(model) {
                                TypeEditProjector(
                                    model = model.type,
                                    localization = localization,
                                )
                            }
                            projector.Content()
                        }

                        type.fold(
                            ifEntry = { entry ->
                                EntryEditProjector(
                                    model = entry,
                                    localization = localization,
                                    amountFormatter = amountFormatter,
                                    budgetRepository = budgetRepository,
                                ).Cells()
                            },
                            ifTransfer = { transfer ->
                                TransferEditProjector(
                                    model = transfer,
                                    localization = localization,
                                    amountFormatter = amountFormatter,
                                    budgetRepository = budgetRepository,
                                ).Cells()
                            },
                        )

                        records?.let { recordsNotNull ->
                            separator(key = "records_separator")
                            val selected = recordsNotNull.selected
                            cells(
                                items = recordsNotNull,
                                key = { it },
                            ) { record ->
                                val projector = remember(record) {
                                    RecordEditProjector(
                                        model = record,
                                        localization = localization,
                                        amountFormatter = amountFormatter,
                                        budgetRepository = budgetRepository,
                                    )
                                }
                                projector.Content(
                                    selected = record === selected,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                },
                actions = {
                    val saveAction = model
                        .goBackDelegate
                        .saveOrInactive
                        .collectAsState()
                        .value
                        ?.collectAsState()
                        ?.value
                    SButton(
                        actionOrElseOrDisabled = saveAction,
                        titleOrIcon = TitleOrIcon.Both(
                            title = localization.save,
                            icon = Drawable.Vector(Icons.Default.Save),
                        ),
                    )
                },
            )
        }
    }
}
