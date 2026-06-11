package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.CreateBudgetModel

class CreateBudgetProjector(
    private val model: CreateBudgetModel,
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
            title = { SText((dependencies.localization.budgets)) },
        ) {
            SLazyTable(
                orientation = Orientation.Vertical,
            ) {
                cell(key = "new") {
                    SPanel(
                        modifier = Modifier.fillMaxWidth(),
                        importanceToActivate = null,
                        actionOrElseOrDisabled = model.createNewBudget.collectAsState().value,
                    ) {
                        SItem(
                            startAccessory = { SIcon(Drawable.Vector(Icons.Default.Add)) }
                        ) {
                            SText(dependencies.localization.createNewBudget)
                        }
                    }
                }
                cell(key = "demo") {
                    SPanel(
                        modifier = Modifier.fillMaxWidth(),
                        importanceToActivate = null,
                        actionOrElseOrDisabled = model.createDemoBudget.collectAsState().value,
                    ) {
                        SItem(
                            startAccessory = { SIcon(Drawable.Vector(Icons.Default.Science)) }
                        ) {
                            SText(dependencies.localization.createDemoBudget)
                        }
                    }
                }
            }
        }
    }
}