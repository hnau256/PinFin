package hnau.pinfin.projector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
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
import hnau.common.app.model.goback.GlobalGoBackHandler
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.NavigationIcon
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.model.AccountModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.account_settings
import hnau.pinfin.projector.resources.hide_if_amount_is_zero
import hnau.pinfin.projector.resources.name
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class AccountProjector(
    scope: CoroutineScope,
    private val model: AccountModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies.globalGoBackHandler.resolve(scope)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.account_settings)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                    actions = { SaveAction() },
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalDisplayPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                val focusRequester = remember { FocusRequester() }
                TextInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalDisplayPadding()
                        .focusRequester(focusRequester),
                    value = model.title,
                    isError = !model.titleIsCorrect.collectAsState().value,
                    label = { Text(stringResource(Res.string.name)) },
                )
                val hideIfAmountIsZero = model.hideIfAmountIsZero.collectAsState().value
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { model.hideIfAmountIsZero.value = !hideIfAmountIsZero },
                    headlineContent = { Text(stringResource(Res.string.hide_if_amount_is_zero)) },
                    trailingContent = {
                        Switch(
                            checked = hideIfAmountIsZero,
                            onCheckedChange = { model.hideIfAmountIsZero.value = it },
                        )
                    }
                )
            }
        }
    }

    @Composable
    private fun RowScope.SaveAction() {
        val saveFlow by model.save.collectAsState()
        val save = saveFlow?.collectAsState()?.value
        val isSaving = saveFlow != null && save == null
        IconButton(
            enabled = save != null,
            onClick = { save?.invoke() },
        ) {
            when (isSaving) {
                true -> CircularProgressIndicator()
                false -> Icon(Icons.Filled.Save)
            }
        }
    }
}