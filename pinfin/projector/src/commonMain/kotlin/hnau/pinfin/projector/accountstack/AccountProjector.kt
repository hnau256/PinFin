package hnau.pinfin.projector.accountstack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import hnau.common.app.projector.uikit.FullScreen
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.TopBarAction
import hnau.common.app.projector.uikit.TopBarTitle
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.collectAsMutableAccessor
import hnau.common.kotlin.ifNull
import hnau.pinfin.model.accountstack.AccountModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.account_settings
import hnau.pinfin.projector.resources.hide_if_amount_is_zero
import hnau.pinfin.projector.resources.hue
import hnau.pinfin.projector.resources.icon
import hnau.pinfin.projector.resources.name
import hnau.pinfin.projector.utils.HueSlider
import hnau.pinfin.projector.utils.image
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class AccountProjector(
    scope: CoroutineScope,
    private val model: AccountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        FullScreen(
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle { Text(stringResource(Res.string.account_settings)) }
                    SaveAction()
                }
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                item(
                    key = "title"
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        overlineContent = {
                            Text(stringResource(Res.string.name))
                        },
                        headlineContent = {
                            val focusRequester = remember { FocusRequester() }
                            TextInput(
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                value = model.title,
                                isError = !model.titleIsCorrect.collectAsState().value,
                            )
                        },
                    )
                }
                item(
                    key = "hue",
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        overlineContent = {
                            Text(stringResource(Res.string.hue))
                        },
                        headlineContent = {
                            HueSlider(
                                value = model.hue,
                                modifier = Modifier
                                    .fillMaxWidth(),
                            )
                        },
                    )
                }
                item(
                    key = "icon",
                ) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { model.chooseIcon() },
                        headlineContent = {
                            Text(stringResource(Res.string.icon))
                        },
                        trailingContent = {
                            Icon(
                                icon = model
                                    .icon
                                    .collectAsState()
                                    .value
                                    ?.image
                                    .ifNull { Icons.Filled.Image }
                            )
                        }
                    )
                }
                item(
                    key = "hide_if_amount_if_zero",
                ) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { model.chooseIcon() },
                        headlineContent = {
                            Text(stringResource(Res.string.hide_if_amount_is_zero))
                        },
                        trailingContent = {
                            var hideIfAccountIsZero by model.hideIfAmountIsZero.collectAsMutableAccessor()
                            Switch(
                                checked = hideIfAccountIsZero,
                                onCheckedChange = { hideIfAccountIsZero = it },
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun SaveAction() {
        val saveFlow by model.save.collectAsState()
        val save = saveFlow?.collectAsState()?.value
        val isSaving = saveFlow != null && save == null
        TopBarAction(
            onClick = save,
        ) {
            when (isSaving) {
                true -> CircularProgressIndicator()
                false -> Icon(Icons.Filled.Save)
            }
        }
    }
}