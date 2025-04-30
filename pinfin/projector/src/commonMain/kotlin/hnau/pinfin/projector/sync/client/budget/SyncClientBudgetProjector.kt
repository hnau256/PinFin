package hnau.pinfin.projector.sync.client.budget

import androidx.compose.runtime.Composable
import hnau.pinfin.model.sync.client.budget.SyncClientBudgetModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncClientBudgetProjector(
    scope: CoroutineScope,
    model: SyncClientBudgetModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }


    @Composable
    fun Content() {

    }
}