package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import arrow.optics.copy
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.utils.Importance
import org.hnau.commons.app.projector.fractal.utils.Saturation
import org.hnau.commons.app.projector.fractal.utils.activateIfNeed
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                STable(
                    orientation = Orientation.Vertical,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    model
                        .items
                        .collectAsState()
                        .value
                        .fastForEach { item ->
                            SCell {
                                SPanel(
                                    actionOrElseOrDisabled = when (val state = item.state) {
                                        is BudgetSwitchModel.Item.State.NotSelected -> state.select.collectAsState().value
                                        BudgetSwitchModel.Item.State.Selected -> null
                                    }
                                ) {
                                    Item(
                                        item = item,
                                    )
                                }
                            }
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
        FContext(
            update = {
                copy(
                    mood = mood.activateIfNeed(
                        selected.ifTrue { Importance.default },
                    )
                )
            }
        ) {
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
}