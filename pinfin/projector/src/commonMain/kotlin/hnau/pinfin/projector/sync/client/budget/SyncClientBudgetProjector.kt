package hnau.pinfin.projector.sync.client.budget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.ErrorPanel
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.pinfin.model.sync.client.budget.SyncClientBudgetModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.back
import hnau.pinfin.projector.butget_was_synchronized
import hnau.pinfin.projector.error_while_budget_synchronization
import hnau.pinfin.projector.try_again
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class SyncClientBudgetProjector(
    scope: CoroutineScope,
    private val model: SyncClientBudgetModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

    }


    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        model
            .resultOrLoading
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.padding(contentPadding),
                transitionSpec = TransitionSpec.crossfade(),
            ) { result ->
                when (result) {
                    is SyncClientBudgetModel.Result.Error -> ErrorPanel(
                        modifier = Modifier.fillMaxSize(),
                        title = { Text(stringResource(Res.string.error_while_budget_synchronization)) },
                        button = {
                            Button(
                                onClick = result.tryAgain,
                                content = { Text(stringResource(Res.string.try_again)) }
                            )
                        }
                    )

                    is SyncClientBudgetModel.Result.Success -> ErrorPanel(
                        modifier = Modifier.fillMaxSize(),
                        title = { Text(stringResource(Res.string.butget_was_synchronized)) },
                        button = {
                            Button(
                                onClick = result.goBack,
                                content = { Text(stringResource(Res.string.back)) }
                            )
                        }
                    )
                }
            }
    }
}