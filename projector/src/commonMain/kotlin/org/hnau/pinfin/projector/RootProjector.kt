package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.backbutton.BackButtonHost
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import org.hnau.pinfin.projector.utils.formatter.datetime.JavaDateTimeFormatter

class RootProjector(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: RootModel,
) {

    @Pipe
    interface Dependencies {

        fun loadBudgets(
            dateTimeFormatter: DateTimeFormatter,
            amountFormatter: AmountFormatter,
        ): LoadBudgetsProjector.Dependencies

        companion object
    }

    /*private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )*/

    private val loadBudgets = LoadBudgetsProjector(
        scope = scope,
        dependencies = dependencies.loadBudgets(
            //bubblesShower = bubblesHolder,
            dateTimeFormatter = JavaDateTimeFormatter(), //TODO
            amountFormatter = AmountFormatter.test,
        ),
        model = model.loadBudgets,
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            //LocalDensity provides Density(LocalDensity.current.density * 1.1f),
        ) {
            BackButtonHost(
                contentPadding = contentPadding,
                goBackHandler = model.goBackHandler
            ) { contentPadding ->
                loadBudgets.Content(
                    contentPadding = contentPadding,
                )
            }
            //bubblesHolder.Content()
        }
    }
}