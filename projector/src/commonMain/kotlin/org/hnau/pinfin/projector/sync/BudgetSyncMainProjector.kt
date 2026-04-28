package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.onClick
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.sync.BudgetSyncMainModel
import org.hnau.pinfin.model.sync.SyncConfig
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth

class BudgetSyncMainProjector(
    private val model: BudgetSyncMainModel,
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
                    TopBarTitle { Text(dependencies.localization.synchronization) }
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
            ConfigCard()
        }
    }

    @Composable
    private fun ConfigCard() {
        Card(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = Dimens.horizontalDisplayPadding,
            ),
        ) {
            model
                .config
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Configs",
                    contentKey = { it != null },
                    transitionSpec = TransitionSpec.horizontal(),
                ) { configsOrNull ->
                    configsOrNull.foldNullable(
                        ifNull = { NoConfig() },
                        ifNotNull = { configs ->
                            Configs(
                                configs = configs,
                            )
                        }
                    )
                }
        }
    }

    @Composable
    private fun Configs(
        configs: StateFlow<SyncConfig>,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = dependencies.localization.synchronizationSettings,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            configs
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Config",
                    contentKey = { it },
                    transitionSpec = TransitionSpec.horizontal(),
                ) { config ->
                    Config(
                        config = config,
                    )
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = Dimens.smallSeparation,
                    alignment = Alignment.End,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val remove = model.removeConfig.collectAsState().value.onClick
                OutlinedButton(
                    onClick = { remove?.invoke() },
                    enabled = remove != null,
                ) {
                    Icon(Icons.Default.Delete)
                }

                Button(
                    onClick = model.openConfig,
                ) {
                    Icon(Icons.Default.Settings)
                    Text(dependencies.localization.doConfig)
                }
            }
        }
    }

    @Composable
    private fun Config(
        config: SyncConfig,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            val items = remember(config) {
                with(dependencies.localization) {
                    listOf(
                        serverHost to config.host.host,
                        httpScheme to config.scheme.name,
                    )
                }
            }
            items.fastForEach { (title, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                ) {
                    Text(
                        text = "$title:",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            }
        }
    }

    @Composable
    private fun NoConfig() {
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = Dimens.separation,
                vertical = Dimens.smallSeparation,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = dependencies.localization.synchronizationSettingsNotExists,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Start,
            )
            Button(
                onClick = model.openConfig,
            ) {
                Icon(Icons.Default.Add)
                Text(dependencies.localization.create)
            }
        }
    }
}