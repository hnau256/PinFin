package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.DateChooseModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter

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
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(dependencies.localization.date)
            },
        ) {
            ReadOnlyValue(
                text = dependencies.dateTimeFormatter.formatDate(date.value),
                onClick = model.navigateContext.requestFocus,
            )
        }
    }
}
