package hnau.pinfin.projector.sync.client.budget

import androidx.compose.runtime.Composable
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncClientLoadBudgetProjector(
    scope: CoroutineScope,
    model: SyncClientLoadBudgetModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }


    @Composable
    fun Content() {

    }
}