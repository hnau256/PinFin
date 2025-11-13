package hnau.pinfin.projector.budget.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import hnau.common.app.projector.uikit.AlertDialogContent
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TopBarDefaults
import hnau.common.app.projector.uikit.progressindicator.InProgress
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifFalse
import hnau.pinfin.model.budget.config.BudgetConfigModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.budget_name
import hnau.pinfin.projector.resources.categories
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.remove_budget
import hnau.pinfin.projector.resources.to_budgets_list
import hnau.pinfin.projector.resources.yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class BudgetConfigProjector(
    private val model: BudgetConfigModel,
) {


    @Composable
    fun Content(
        bottomInset: Dp,
    ) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                TopBarDefaults.height
        LazyColumn(
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomInset,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            button(
                titleRes = Res.string.to_budgets_list,
                onClick = model::openBudgetsList,
                icon = Icons.AutoMirrored.Filled.MenuOpen,
            )
            name()
            button(
                titleRes = Res.string.remove_budget,
                onClick = model::removeClick,
                icon = Icons.Filled.Delete,
            )
            button(
                titleRes = Res.string.categories,
                onClick = model::openCategories,
                icon = Icons.Filled.Interests,
            )
        }
        RemoveDialog()
        InProgress(model.inProgress)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RemoveDialog() {
        model.removeDialogVisible.collectAsState().value.ifFalse { return }
        BasicAlertDialog(
            onDismissRequest = model::removeCancel
        ) {
            AlertDialogContent(
                title = { Text(stringResource(Res.string.remove_budget)) },
                confirmButton = {
                    TextButton(
                        onClick = model::removeConfirm,
                        content = { Text(stringResource(Res.string.yes)) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::removeCancel,
                        content = { Text(stringResource(Res.string.no)) },
                    )
                }
            )
        }
    }

    @Composable
    private fun Name(
        name: BudgetConfigModel.NameOrEdit.Name,
    ) {
        ListItem(
            overlineContent = { Text(stringResource(Res.string.budget_name)) },
            headlineContent = { Text(name.name) },
            trailingContent = {
                IconButton(
                    onClick = name.edit,
                ) {
                    Icon(Icons.Filled.Edit)
                }
            },
            leadingContent = { Icon(Icons.Filled.Badge) },
        )
    }

    @Composable
    private fun Edit(
        edit: BudgetConfigModel.NameOrEdit.Edit,
    ) {
        ListItem(
            headlineContent = {
                val focusRequester = remember { FocusRequester() }
                TextInput(
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = edit.input,
                    keyboardActions = KeyboardActions { edit.save.value?.invoke() },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            },
            leadingContent = {
                IconButton(
                    onClick = edit.cancel,
                ) {
                    Icon(Icons.Filled.Clear)
                }
            },
            trailingContent = {

                edit
                    .save
                    .collectAsState()
                    .value
                    .StateContent(
                        transitionSpec = TransitionSpec.crossfade(),
                        label = "SaveBudgetNameOrSaving",
                        contentKey = { it != null },
                    ) { saveOrNull ->
                        saveOrNull.foldNullable(
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

    private fun LazyListScope.name() {
        item(
            key = "name",
        ) {
            model
                .nameOrEdit
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = TransitionSpec.vertical(),
                    label = "BudgetNameOrEdit",
                    contentKey = {
                        when (it) {
                            is BudgetConfigModel.NameOrEdit.Name -> 0
                            is BudgetConfigModel.NameOrEdit.Edit -> 1
                        }
                    },
                ) { nameOrEdit ->
                    when (nameOrEdit) {
                        is BudgetConfigModel.NameOrEdit.Name -> Name(nameOrEdit)
                        is BudgetConfigModel.NameOrEdit.Edit -> Edit(nameOrEdit)
                    }
                }
        }
    }

    private fun LazyListScope.button(
        icon: ImageVector,
        titleRes: StringResource,
        onClick: () -> Unit,
    ) {
        item(
            key = "button_${titleRes.key}",
        ) {
            ListItem(
                leadingContent = { Icon(icon = icon) },
                modifier = Modifier.clickable(onClick = onClick),
                headlineContent = { Text(stringResource(titleRes)) },
                trailingContent = { Icon(Icons.Filled.ChevronRight) }
            )
        }
    }
}