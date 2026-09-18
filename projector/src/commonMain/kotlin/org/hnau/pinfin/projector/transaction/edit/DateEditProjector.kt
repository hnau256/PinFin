package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.padding.LocalContentPaddingBox
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.DateChooseModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class DateEditProjector(
    private val model: DateChooseModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val dateTimeFormatter: DateTimeFormatter
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val date by model.dateEditable.collectAsState()
        SFocusablePanel(
            modifier = modifier,
            navigateContext = model.navigateContext,
            notFocusedContent = {
                SItem(
                    topAccessory = {
                        SText(dependencies.localization.date)
                    },
                ) {
                    SText(
                        text = dependencies.dateTimeFormatter.formatDate(date.value),
                    )
                }
            },
            focusedContent = {
                LocalContentPaddingBox {
                    val state = rememberDatePickerState(
                        initialSelectedDateMillis = model
                            .dateEditable
                            .collectAsState()
                            .value
                            .value
                            .atStartOfDayIn(TimeZone.currentSystemDefault())
                            .plus(0.5.days)
                            .toEpochMilliseconds(),
                    )
                    val selected = state.selectedDateMillis
                    LaunchedEffect(selected) {
                        selected
                            ?.let(Instant.Companion::fromEpochMilliseconds)
                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                            ?.date
                            ?.let(model::updateDate)
                    }
                    DatePicker(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.Transparent,
                        )
                    )
                }
            }
        )
    }
}
