package hnau.pinfin.projector.bidget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.model.budget.AnalyticsModel
import hnau.pinfin.model.budget.BudgetConfigModel
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