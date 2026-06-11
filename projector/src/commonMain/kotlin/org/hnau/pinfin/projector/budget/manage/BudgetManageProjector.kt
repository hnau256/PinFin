package org.hnau.pinfin.projector.budget.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STitleOrIcon
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.context.LocalFContext
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.padding.LocalContentPadding
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.STableScope
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyCellScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.Subtable
import org.hnau.commons.app.projector.fractal.table.lazy.separator
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import kotlin.invoke


class BudgetManageProjector(
    private val scope: CoroutineScope,
    private val model: BudgetManageModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val dateTimeFormatter: DateTimeFormatter

        fun remove(): BudgetManageRemoveProjector.Dependencies

        fun share(): BudgetManageShareProjector.Dependencies
    }

    private val remove = BudgetManageRemoveProjector(
        scope = scope,
        model = model.remove,
        dependencies = dependencies.remove(),
    )

    private val share = BudgetManageShareProjector(
        model = model.share,
        dependencies = dependencies.share(),
    )


    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            CompositionLocalProvider(
                LocalContentPadding provides PaddingValues(
                    top = TopBarDefaults.height + TopBarDefaults.separationTop,
                ) +
                        contentPadding +
                        LocalDistance.current.units.paddingValues.vertical.medium
            ) {
                SLazyTable(
                    orientation = Orientation.Vertical,
                ) {
                    cell(key = "sync") { Sync() }
                    cell(key = "share") { share.Content() }

                    separator()

                    cell(key = "settings") {
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(model::openSettings),
                            importanceToActivate = null,
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
                    }
                    cell(key = "categories") {
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(model::openCategories),
                            importanceToActivate = null,
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

                    separator()

                    cell(key = "create") {
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(model::openCreateBudget),
                            importanceToActivate = null,
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
                    }
                    cell(key = "switch") {
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(model::openSwitchBudget),
                            importanceToActivate = null,
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
                    }
                    cell(key = "remove") {
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(remove::onRemoveClick),
                            importanceToActivate = null,
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
                    }
                }
            }
            remove.Content()
        }
    }

    @Composable
    private fun SLazyCellScope.Sync() {
        val sync = model.sync

        val syncResult = sync
            .lastCurrentSessionResult
            .collectAsState()
            .value

        FContext(
            update = {
                copy(
                    mood = (syncResult == false).foldBoolean(
                        ifTrue = { Mood.Error },
                        ifFalse = { Mood.Neutral },
                    )
                )
            }
        ) {
            Subtable {
                SCell(
                    modifier = Modifier.weight(1f),
                ) {
                    SPanel {
                        val dateTimeFormatter = dependencies.dateTimeFormatter
                        val localization = dependencies.localization

                        val lastSuccessSync = sync
                            .lastSuccessSync
                            .collectAsState()
                            .value

                        val statistics = sync
                            .statistics
                            .collectAsState()
                            .value
                        SItem(
                            topAccessory = { SText(localization.synchronization) },
                            startAccessory = {
                                SIcon(
                                    Drawable.Vector(
                                        when (syncResult) {
                                            true -> Icons.Default.CloudDone
                                            false -> Icons.Default.Error
                                            null -> Icons.Default.Cloud
                                        }
                                    )
                                )
                            },
                            bottomAccessory = remember(
                                lastSuccessSync,
                                dateTimeFormatter,
                                localization,
                            ) {
                                lastSuccessSync
                                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?.let { lastSuccessSync ->
                                        listOf(
                                            dependencies.dateTimeFormatter.formatDate(
                                                lastSuccessSync.date
                                            ),
                                            dependencies.dateTimeFormatter.formatTime(
                                                lastSuccessSync.time
                                            ),
                                        ).joinToString(
                                            separator = " ",
                                        )
                                    }
                                    ?.let { lastSync ->
                                        {
                                            SText(
                                                text = lastSync,
                                            )
                                        }
                                    }
                            },
                            content = remember(statistics) {
                                val items = listOf(
                                    KeyValue(
                                        Icons.Default.ArrowCircleDown,
                                        "${statistics.appliedUpdates} (+${statistics.receivedUpdates - statistics.appliedUpdates})"
                                    ),
                                    KeyValue(
                                        Icons.Default.ArrowCircleUp,
                                        statistics.sentUpdates.toString()
                                    )
                                ).map { item ->
                                    TitleOrIcon.Both(
                                        title = item.value,
                                        icon = Drawable.Vector(item.key),
                                    )
                                }
                                val result: @Composable () -> Unit = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small)
                                    ) {
                                        items.fastForEach { item ->
                                            STitleOrIcon(
                                                titleOrIcon = item,
                                            )
                                        }
                                    }
                                }
                                result
                            }
                        )
                    }
                }
                SCell {
                    SButton(
                        actionOrElseOrDisabled = sync.sync.collectAsState().value,
                        titleOrIcon = TitleOrIcon.Icon(
                            Drawable.Vector(
                                Icons.Default.Sync
                            )
                        ),
                        importanceToActivate = null,
                    )
                }
            }
        }

    }
}