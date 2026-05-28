package org.hnau.pinfin.projector.budgetsettings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExposureZero
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SElements
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.STable
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.input.InputProjector
import org.hnau.commons.app.projector.fractal.input.createInputProjector
import org.hnau.commons.app.projector.fractal.input.type.toInputProjectorPrototype
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.ProjectorSavableDelegate
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.bidgetsettings.BudgetSettingsModel
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.projector.Localization

class BudgetSettingsProjector(
    private val scope: CoroutineScope,
    private val model: BudgetSettingsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

    }

    private val mainTitle: InputProjector = model
        .mainTitle
        .toInputProjectorPrototype(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        )
        .createInputProjector(
            scope = scope,
            title = dependencies.localization.name,
            icon = Drawable.Vector(Icons.AutoMirrored.Filled.Label),
        ) { _, _ ->
            dependencies.localization.nameShouldNotByEmpty
        }

    private val mainMantissaLength: InputProjector = model
        .mainMantissaLength
        .toInputProjectorPrototype(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        )
        .createInputProjector(
            scope = scope,
            title = dependencies.localization.mantissaLength,
            icon = Drawable.Vector(Icons.Default.ExposureZero),
        ) { _, _ ->
            dependencies.localization.incorrectMantissaLength
        }

    private val syncScheme: InputProjector = model
        .syncScheme
        .toInputProjectorPrototype { scheme ->
            SText(
                text = scheme.name,
            )
        }
        .createInputProjector(
            scope = scope,
            title = dependencies.localization.httpScheme,
            icon = Drawable.Vector(Icons.Default.Security),
        )

    private val syncHost: InputProjector = model
        .syncHost
        .toInputProjectorPrototype(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
        )
        .createInputProjector(
            scope = scope,
            title = dependencies.localization.serverHost,
            icon = Drawable.Vector(Icons.Default.Cloud),
        ) { _, _ ->
            dependencies.localization.hostIsIncorrect
        }

    private val savableDelegate: ProjectorSavableDelegate<BudgetInfo> = ProjectorSavableDelegate(
        scope = scope,
        model = model.savableDelegate,
        notSaved = {
            SCellBox {
                SText(
                    text = dependencies.localization.budgetConfigIsNotSaved,
                    type = SizeType.Large,
                )
            }
        },
        save = dependencies.localization.save,
        edit = dependencies.localization.edit,
        reset = dependencies.localization.reset,
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText(dependencies.localization.budgetConfig) },
        ) { contentPadding ->
            SContentWithActions(
                modifier = Modifier.padding(contentPadding),
                content = {
                    SElements {
                        STable(
                            orientation = Orientation.Vertical,
                        ) {
                            SCellBox(
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                SText(dependencies.localization.budgetConfigMain)
                            }
                            with(mainTitle) { Content() }
                            with(mainMantissaLength) { Content() }
                        }
                        STable(
                            orientation = Orientation.Vertical,
                        ) {
                            SCellBox(
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                SText(dependencies.localization.budgetConfigSync)
                            }
                            with(syncScheme) { Content() }
                            with(syncHost) { Content() }
                        }
                    }

                },
                actions = {
                    Action(
                        actionOrElseOrDisabled = model
                            .savableDelegate
                            .saveOrInactive
                            .collectAsState()
                            .value
                            ?.collectAsState()
                            ?.value,
                        titleOrIcon = TitleOrIcon.Both(
                            title = dependencies.localization.save,
                            icon = Drawable.Vector(Icons.Default.Save),
                        )
                    )
                }
            )
            savableDelegate.Dialog()
        }
    }
}