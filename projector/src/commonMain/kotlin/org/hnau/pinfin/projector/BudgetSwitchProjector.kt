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
                        actionOrElseOrDisabled = when (val state = budget.state) {
                            is BudgetSwitchModel.Item.State.NotSelected -> state.select.collectAsState().value
                            BudgetSwitchModel.Item.State.Selected -> null
                        },
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
        val selected = when (item.state) {
            is BudgetSwitchModel.Item.State.NotSelected -> false
            BudgetSwitchModel.Item.State.Selected -> true
        }
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