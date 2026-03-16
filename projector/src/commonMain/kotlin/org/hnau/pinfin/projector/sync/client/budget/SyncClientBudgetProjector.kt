package org.hnau.pinfin.projector.sync.client.budget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.ErrorPanel
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.pinfin.model.sync.client.budget.SyncClientBudgetModel
import org.hnau.pinfin.projector.Res
import org.hnau.pinfin.projector.back
import org.hnau.pinfin.projector.butget_was_synchronized
import org.hnau.pinfin.projector.error_while_budget_synchronization
import org.hnau.pinfin.projector.try_again
import org.jetbrains.compose.resources.stringResource

class SyncClientBudgetProjector(
    private val model: SyncClientBudgetModel,
) {


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