package org.hnau.pinfin.projector.budget.manage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.table.TableScope
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.manage.BudgetManageShareModel
import org.hnau.pinfin.projector.Localization

class BudgetManageShareProjector(
    private val model: BudgetManageShareModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun TableScope.Content() {
        model
            .state
            .collectAsState()
            .value
            .StateContent(
                label = "shareState",
                contentKey = { it.ordinal },
                transitionSpec = TransitionSpec.remember(
                    showAlignment = Alignment.BottomCenter,
                    hideAlignment = Alignment.TopCenter,
                )
            ) { state ->
                when (state) {
                    is BudgetManageShareModel.State.Closed -> SCellBox(
                        onClick = { state.openAndCopyCode() },
                    ) {
                        SItem(
                            startAccessory = {
                                SIcon(Drawable.Vector(Icons.Default.Share))
                            },
                        ) {
                            SText(
                                dependencies.localization.shareBudget
                            )
                        }
                    }

                    is BudgetManageShareModel.State.Opened -> SCellBox(
                        onClick = { state.copyCode() },
                    ) {
                        SItem(
                            topAccessory = {
                                SText(dependencies.localization.shareBudget)
                            },
                            startAccessory = {
                                SIcon(Drawable.Vector(Icons.Default.Share))
                            },
                            endAccessory = {
                                SIcon(Drawable.Vector(Icons.Default.ContentCopy))
                            },
                            bottomAccessory = {
                                SText(dependencies.localization.shareBudgetInfo)
                            },
                        ) {
                            SText(state.code.collectAsState().value)
                        }
                    }
                }
            }
    }
}