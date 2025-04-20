package hnau.pinfin.client.projector.bidget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.client.model.budget.AnalyticsModel
import hnau.pinfin.client.model.budget.BudgetConfigModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class BudgetConfigProjector(
    scope: CoroutineScope,
    model: BudgetConfigModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {

    }

}