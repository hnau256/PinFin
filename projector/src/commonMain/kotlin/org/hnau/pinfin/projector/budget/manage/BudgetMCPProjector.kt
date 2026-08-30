package org.hnau.pinfin.projector.budget.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.tmapps.konnection.Konnection
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

    private val konnection: Konnection by lazy {
        Konnection.createInstance(ipResolvers = emptyList())
    }

    @Composable
    fun Content() {
        val mcpIsEnabled by model.mcpIsEnabled.collectAsState()
        val addresses by produceState<List<String>>(initialValue = emptyList(), mcpIsEnabled) {
            value = if (mcpIsEnabled) {
                konnection
                    .getInfo()
                    ?.let { info ->
                        buildList {
                            info.ipv4?.let {
                                add("$it:${BudgetMCPModel.MCP_PORT}${BudgetMCPModel.MCP_PATH}")
                            }
                            info.ipv6?.let {
                                add("[$it]:${BudgetMCPModel.MCP_PORT}${BudgetMCPModel.MCP_PATH}")
                            }
                        }
                    }
                    .orEmpty()
            } else {
                emptyList()
            }
        }
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
                bottomAccessory = if (addresses.isEmpty()) {
                    null
                } else {
                    {
                        Column {
                            addresses.forEach { address ->
                                SText(address)
                            }
                        }
                    }
                },
                content = {
                    SText(dependencies.localization.accessByMCP)
                }
            )
        }
    }
}
