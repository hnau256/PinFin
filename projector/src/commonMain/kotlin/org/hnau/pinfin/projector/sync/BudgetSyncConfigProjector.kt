package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SContentWithActions
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
import org.hnau.pinfin.model.sync.BudgetSyncConfigModel
import org.hnau.pinfin.model.sync.SyncConfig
import org.hnau.pinfin.projector.Localization

class BudgetSyncConfigProjector(
    private val scope: CoroutineScope,
    private val model: BudgetSyncConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

    }

    private val scheme: InputProjector = model
        .scheme
        .toInputProjectorPrototype { scheme ->
            SText(
                text = scheme.title,
            )
        }
        .run {
            val titleWithIcon = SyncUICommons.createSchemeTitleWithIcon(
                localization = dependencies.localization,
            )
            createInputProjector(
                scope = scope,
                title = titleWithIcon.title,
                icon = titleWithIcon.icon,
            )
        }

    private val host: InputProjector = model
        .host
        .toInputProjectorPrototype(
            imeAction = ImeAction.Done,
        )
        .run {
            val titleWithIcon = SyncUICommons.createHostTitleWithIcon(
                localization = dependencies.localization,
            )
            createInputProjector(
                scope = scope,
                title = titleWithIcon.title,
                icon = titleWithIcon.icon,
            ) { _, _ ->
                dependencies
                    .localization
                    .hostIsIncorrect
            }
        }

    private val savableDelegate: ProjectorSavableDelegate<SyncConfig> = ProjectorSavableDelegate(
        scope = scope,
        model = model.savableDelegate,
        notSaved = {
            SCellBox {
                SText(
                    text = dependencies.localization.synchronizationSettingsNotSaved,
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
            title = { SText(dependencies.localization.synchronizationSettings) },
        ) { contentPadding ->
            SContentWithActions(
                modifier = Modifier.padding(contentPadding),
                content = {
                    STable(
                        orientation = Orientation.Vertical,
                    ) {
                        with(scheme) { Content() }
                        with(host) { Content() }
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
                            title = "Save",
                            icon = Drawable.Vector(Icons.Default.Save),
                        )
                    )
                }
            )
            savableDelegate.Dialog()
        }
    }
}