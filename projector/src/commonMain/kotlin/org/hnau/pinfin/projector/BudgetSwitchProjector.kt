package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.ifTrue
import org.hnau.pinfin.model.BudgetSwitchModel
import org.hnau.pinfin.model.fold

class BudgetSwitchProjector(
    private val model: BudgetSwitchModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText((dependencies.localization.switchBudget)) },
        ) {
            val budgets = model
                .items
                .collectAsState()
                .value
            SLazyTable(
                orientation = Orientation.Vertical,
            ) {
                cells(
                    items = budgets,
                ) { budget ->
                    SPanel(
                        actionOrElseOrDisabled = budget.state.fold(
                            ifNotSelected = { select ->
                                select.collectAsState().value
                            },
                            ifSelected = { null },
                        ),
                        importanceToActivate = null,
                    ) {
                        Item(
                            item = budget,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Item(
        item: BudgetSwitchModel.Item,
    ) {
        val selected = item.state.fold(
            ifNotSelected = { _ -> false },
            ifSelected = { true },
        )
        SItem(
            endAccessory = selected.ifTrue {
                {
                    SIcon(
                        drawable = Drawable.Vector(Icons.Default.Check)
                    )
                }
            },
        ) {
            SText(
                text = item.title.collectAsState().value,
            )
        }
    }
}