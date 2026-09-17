package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.pinfin.model.transaction.edit.DateChooseModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter

class DateEditProjector(
    private val model: DateChooseModel,
    private val localization: Localization,
    private val dateTimeFormatter: DateTimeFormatter,
) {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val date = model.dateEditable.collectAsState().value
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(localization.date)
            },
        ) {
            ReadOnlyValue(
                text = dateTimeFormatter.formatDate(date.value),
                onClick = model.navigateContext.requestFocus,
            )
        }
    }
}
