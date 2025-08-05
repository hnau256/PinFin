package hnau.pinfin.projector.transaction.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.part.DateModel
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class DateProjector(
    scope: CoroutineScope,
    private val model: DateModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            ItemsRow {
                Icon(Icons.Filled.CalendarMonth)
                Text(
                    text = model
                        .date
                        .collectAsState()
                        .value
                        .let(dependencies.dateTimeFormatter::formatDate),
                )
            }
        }
    }
}