package hnau.pinfin.projector

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import hnau.common.app.projector.uikit.backbutton.BackButtonProjector
import hnau.pinfin.model.RootModel
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pinfin.projector.utils.formatter.datetime.JavaDateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class RootProjector(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    model: RootModel,
) {

    @Pipe
    interface Dependencies {

        fun loadBudgets(
            dateTimeFormatter: DateTimeFormatter,
            amountFormatter: AmountFormatter,
            backButtonWidth: BackButtonWidth,
        ): LoadBudgetsProjector.Dependencies

        companion object
    }

    /*private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )*/

    private val backButton = BackButtonProjector(
        scope = scope,
        goBackHandler = model.goBackHandler,
    )

    private val loadBudgets = LoadBudgetsProjector(
        scope = scope,
        dependencies = dependencies.loadBudgets(
            //bubblesShower = bubblesHolder,
            dateTimeFormatter = JavaDateTimeFormatter(), //TODO
            amountFormatter = AmountFormatter.test,
            backButtonWidth = BackButtonWidth.create(backButton),
        ),
        model = model.loadBudgets,
    )

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            //LocalDensity provides Density(LocalDensity.current.density * 1.1f),
        ) {
            loadBudgets.Content()
            backButton.Content()
            //bubblesHolder.Content()
        }
    }
}