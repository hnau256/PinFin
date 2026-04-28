package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarAction
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.onClick
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.sync.BudgetSyncConfigModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth

class BudgetSyncConfigProjector(
    private val model: BudgetSyncConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val backButtonWidth: BackButtonWidth
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        FullScreen(
            contentPadding = contentPadding,
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle { Text(dependencies.localization.synchronizationSettings) }
                    TopBarAction(
                        onClick = model
                            .savableDelegate
                            .saveOrInactive
                            .collectAsState()
                            .value
                            ?.collectAsState()
                            ?.value
                            ?.onClick
                    ) {
                        Icon(
                            Icons.Default.Save
                        )
                    }
                }
            },
        ) { contentPadding ->
            ContentMain(
                contentPadding = contentPadding,
            )
        }
    }

    @Composable
    private fun ContentMain(
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(vertical = Dimens.separation),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            TextInput(
                value = model.hostInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                ),
                label = { Text(dependencies.localization.serverHost) }
            )
        }
    }
}