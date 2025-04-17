package hnau.pinfin.client.projector

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import hnau.common.compose.uikit.bubble.BubblesShower
import hnau.common.compose.uikit.bubble.Content
import hnau.common.compose.uikit.bubble.SharedBubblesHolder
import hnau.pinfin.client.model.RootModel
import hnau.pinfin.client.projector.utils.AmountFormatter
import hnau.pinfin.client.projector.utils.DateTimeFormatter
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class RootProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    model: RootModel,
) {

    @Shuffle
    interface Dependencies {

        fun loadBudgets(
            bubblesShower: BubblesShower,
            dateTimeFormatter: DateTimeFormatter,
            amountFormatter: AmountFormatter,
        ): LoadBudgetsProjector.Dependencies
    }

    private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )

    private val loadBudgets = LoadBudgetsProjector(
        scope = scope,
        dependencies = dependencies.loadBudgets(
            bubblesShower = bubblesHolder,
            dateTimeFormatter = DateTimeFormatter.test, //TODO
            amountFormatter = AmountFormatter.test,
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
            bubblesHolder.Content()
        }
    }
}