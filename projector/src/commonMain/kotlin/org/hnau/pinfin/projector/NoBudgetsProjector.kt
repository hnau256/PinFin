package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.progressindicator.InProgress
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.NoBudgetsModel

class NoBudgetsProjector(
    private val model: NoBudgetsModel,
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
        SScreen(
            contentPadding = contentPadding,
            title = { SText((dependencies.localization.budgets)) },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(
                    space = Dimens.smallSeparation,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = model::createNewBudget,
                ) {
                    Text(dependencies.localization.createNewBudget)
                }
                OutlinedButton(
                    onClick = model::createDemoBudget,
                ) {
                    Text(dependencies.localization.createDemoBudget)
                }
            }
            InProgress(
                inProgress = model.inProgress,
            )
        }
    }
}