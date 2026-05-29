package org.hnau.pinfin.projector.budget.manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.STable
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.LocalFContext
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.projector.Localization


class BudgetManageProjector(
    private val scope: CoroutineScope,
    private val model: BudgetManageModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun remove(): BudgetManageRemoveProjector.Dependencies
    }

    private val remove = BudgetManageRemoveProjector(
        scope = scope,
        model = model.remove,
        dependencies = dependencies.remove(),
    )


    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Box {
            STable(
                orientation = Orientation.Vertical,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .padding(LocalFContext.current.distance.units.paddingValues.vertical.medium)
                    .padding(top = TopBarDefaults.height + TopBarDefaults.separationTop)
            ) {
                SCellBox(
                    onClick = { /*Open create new budget screen*/ },
                ) {
                    SItem(
                        startAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.PostAdd))
                        },
                        endAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.ChevronRight))
                        },
                    ) {
                        SText(
                            dependencies.localization.addBudget
                        )
                    }
                }
                SCellBox(
                    onClick = { /*Open switch budget screen*/ },
                ) {
                    SItem(
                        startAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.SwapHoriz))
                        },
                        endAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.ChevronRight))
                        },
                    ) {
                        SText(
                            dependencies.localization.switchBudget
                        )
                    }
                }
                SCellBox(
                    onClick = model::openSettings,
                ) {
                    SItem(
                        startAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.Settings))
                        },
                        endAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.ChevronRight))
                        },
                    ) {
                        SText(
                            dependencies.localization.settings
                        )
                    }
                }
                SCellBox(
                    onClick = remove::onRemoveClick,
                ) {
                    SItem(
                        startAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.Delete))
                        },
                    ) {
                        SText(
                            dependencies.localization.removeBudget
                        )
                    }
                }
                SCellBox(
                    onClick = model::openCategories,
                ) {
                    SItem(
                        startAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.Interests))
                        },
                        endAccessory = {
                            SIcon(Drawable.Vector(Icons.Default.ChevronRight))
                        },
                    ) {
                        SText(
                            dependencies.localization.categories
                        )
                    }
                }
            }
        }
        remove.Content()
    }
}