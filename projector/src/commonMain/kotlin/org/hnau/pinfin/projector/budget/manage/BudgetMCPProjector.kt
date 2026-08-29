package org.hnau.pinfin.projector.budget.manage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.hnau.commons.app.projector.fractal.SCheckBox
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.pinfin.model.budget.manage.BudgetMCPModel
import org.hnau.pinfin.projector.Localization

class BudgetMCPProjector(
    private val model: BudgetMCPModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content() {
        val mcpIsEnabled by model.mcpIsEnabled.collectAsState()
        SPanel(
            actionOrElseOrDisabled = ActionOrElse.instant {
                model.mcpIsEnabled.value = !mcpIsEnabled
            },
            importanceToActivate = null,
        ) {
            SItem(
                startAccessory = { SIcon(Drawable.Vector(Icons.Default.Public)) },
                endAccessory = {
                    SCheckBox(
                        isChecked = mcpIsEnabled,
                    )
                },
                content = {
                    SText(dependencies.localization.accessByMCP)
                }
            )
        }
    }
}