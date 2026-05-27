package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SActions
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SElements
import org.hnau.commons.app.projector.fractal.SMainWithAdditional
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.STable
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STitleOrIcon
import org.hnau.commons.app.projector.fractal.context.UpdateFContext
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.fractal.utils.Saturation
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.uikit.table.Subtable
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.pinfin.model.sync.BudgetSyncMainModel
import org.hnau.pinfin.projector.Localization

class BudgetSyncMainProjector(
    scope: CoroutineScope,
    private val model: BudgetSyncMainModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

    }

    private val configItems: StateFlow<List<KeyValue<TitleOrIcon.Both, String>>> = model
        .config
        .mapState(scope) { config ->
            listOf(
                SyncUICommons.createSchemeTitleWithIcon(
                    localization = dependencies.localization,
                ) to config.scheme.title,
                SyncUICommons.createHostTitleWithIcon(
                    localization = dependencies.localization,
                ) to config.host.host,
            ).map { (title, value) ->
                KeyValue(title, value)
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText(dependencies.localization.synchronization) },
        ) { contentPadding ->
            SContentWithActions(
                modifier = Modifier.padding(contentPadding),
                content = {
                    SMainWithAdditional(
                        main = { Box {} },
                        additional = {
                            SElements {
                                STable(
                                    orientation = Orientation.Vertical,
                                ) {
                                    SCellBox(
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        UpdateFContext(
                                            saturation = Saturation.Active,
                                        ) {
                                            SText(
                                                text = dependencies.localization.config,
                                            )
                                        }
                                    }
                                    Subtable {
                                        val rowHeight = 56.dp
                                        val rows = configItems
                                            .collectAsState()
                                            .value
                                        Subtable {
                                            rows.forEach { (titleOrIcon) ->
                                                SCellBox(
                                                    modifier = Modifier.height(rowHeight),
                                                    contentAlignment = Alignment.CenterStart,
                                                ) {
                                                    STitleOrIcon(
                                                        titleOrIcon = titleOrIcon,
                                                    )
                                                }
                                            }
                                        }
                                        Subtable(
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            rows.forEach { (_, value) ->
                                                SCellBox(
                                                    modifier = Modifier.height(rowHeight),
                                                    contentAlignment = Alignment.CenterEnd,
                                                ) {
                                                    UpdateFContext(
                                                        saturation = Saturation.Active,
                                                    ) {
                                                        SText(value)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    SActions {
                                        Action(
                                            actionOrElseOrDisabled = model
                                                .openConfig
                                                .collectAsState()
                                                .value
                                                ?.let(ActionOrElse.Companion::instant),
                                            titleOrIcon = TitleOrIcon.Both(
                                                title = dependencies.localization.edit,
                                                icon = Drawable.Vector(Icons.Default.Edit),
                                            ),
                                            mood = Mood.Secondary,
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
                actions = {
                    Action(
                        actionOrElseOrDisabled = model.sync.collectAsState().value,
                        titleOrIcon = TitleOrIcon.Both(
                            title = dependencies.localization.synchronization,
                            icon = Drawable.Vector(Icons.Default.Sync),
                        ),
                    )
                }
            )
        }
    }
}