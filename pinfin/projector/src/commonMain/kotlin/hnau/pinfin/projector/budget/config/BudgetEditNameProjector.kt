package hnau.pinfin.projector.budget.config

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.utils.Icon
import hnau.common.kotlin.fold
import hnau.pinfin.model.budget.config.BudgetEditNameModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class BudgetEditNameProjector(
    scope: CoroutineScope,
    private val model: BudgetEditNameModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        ListItem(
            headlineContent = {
                val focusRequester = remember { FocusRequester() }
                TextInput(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = model.name,
                    keyboardActions = KeyboardActions { model.save.value?.invoke() },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            },
            leadingContent = {
                IconButton(
                    onClick = model::cancel,
                ) {
                    Icon(Icons.Filled.Clear)
                }
            },
            trailingContent = {

                model
                    .save
                    .collectAsState()
                    .value
                    .StateContent(
                        transitionSpec = TransitionSpec.crossfade(),
                        label = "SaveBudgetNameOrSaving",
                        contentKey = { it != null },
                    ) { saveOrNull ->
                        saveOrNull.fold(
                            ifNull = { CircularProgressIndicator() },
                            ifNotNull = { save ->
                                IconButton(
                                    onClick = save,
                                ) {
                                    Icon(Icons.Filled.Done)
                                }
                            }
                        )
                    }
            },
        )
    }
}