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
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.sync.client.budget.SyncClientBudgetModel
import org.hnau.pinfin.projector.Localization

class SyncClientBudgetProjector(
    private val model: SyncClientBudgetModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
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
                        title = { Text(dependencies.localization.errorWhileBudgetSynchronization) },
                        button = {
                            Button(
                                onClick = result.tryAgain,
                                content = { Text(dependencies.localization.tryAgain) }
                            )
                        }
                    )

                    is SyncClientBudgetModel.Result.Success -> ErrorPanel(
                        modifier = Modifier.fillMaxSize(),
                        title = { Text(dependencies.localization.budgetWasSynchronized) },
                        button = {
                            Button(
                                onClick = result.goBack,
                                content = { Text((dependencies.localization.back)) }
                            )
                        }
                    )
                }
            }
    }
}