package org.hnau.pinfin.projector.budget.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SCell
import org.hnau.commons.app.projector.fractal.SCellBox
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.STable
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STitleOrIcon
import org.hnau.commons.app.projector.fractal.context.LocalFContext
import org.hnau.commons.app.projector.fractal.context.UpdateFContext
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.uikit.table.Subtable
import org.hnau.commons.app.projector.uikit.table.TableScope
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter


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
                Sync()
            }
        }
        remove.Content()
    }

    @Composable
    private fun TableScope.Sync() {
        val sync = model.sync

        val isError = sync
            .lastCurrentSessionResult
            .collectAsState()
            .value == false

        UpdateFContext(
            mood = isError.foldBoolean(
                ifTrue = { Mood.Error },
                ifFalse = { Mood.Primary },
            )
        ) {
            Subtable {
                SCellBox(
                    modifier = Modifier.weight(1f),
                ) {
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
                                    Icons.Default.Cloud,
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
                                        dependencies.dateTimeFormatter.formatDate(lastSuccessSync.date),
                                        dependencies.dateTimeFormatter.formatTime(lastSuccessSync.time),
                                    ).joinToString(
                                        separator = " ",
                                        prefix = dependencies
                                            .localization
                                            .lastSynchronization + " ",
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
                                    horizontalArrangement = Arrangement.spacedBy(LocalFContext.current.distance.units.padding.along.small)
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
                SCell { shape ->
                    SButton(
                        shape = shape,
                        actionOrElseOrDisabled = sync.sync.collectAsState().value,
                        titleOrIcon = TitleOrIcon.Icon(
                            Drawable.Vector(
                                Icons.Default.Sync
                            )
                        )
                    )
                }
            }
        }

    }
}