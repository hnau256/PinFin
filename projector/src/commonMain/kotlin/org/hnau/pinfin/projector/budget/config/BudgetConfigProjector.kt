package org.hnau.pinfin.projector.budget.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Sync
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
import org.hnau.commons.app.projector.uikit.AlertDialogContent
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.progressindicator.InProgress
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifFalse
import org.hnau.pinfin.model.budget.config.BudgetConfigModel
import org.hnau.pinfin.projector.Localization


class BudgetConfigProjector(
    private val model: BudgetConfigModel,
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
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(
                top = TopBarDefaults.height,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            button(
                id = "NoBudgets",
                title = dependencies.localization.toBudgetsList,
                onClick = model::openBudgetsList,
                icon = Icons.AutoMirrored.Filled.MenuOpen,
            )
            name()
            button(
                id = "RemoveBudget",
                title = dependencies.localization.removeBudget,
                onClick = model::removeClick,
                icon = Icons.Filled.Delete,
            )
            button(
                id = "Categories",
                title = dependencies.localization.categories,
                onClick = model::openCategories,
                icon = Icons.Filled.Interests,
            )
            button(
                id = "Sync",
                title = dependencies.localization.synchronization,
                onClick = model::openSync,
                icon = Icons.Filled.Sync,
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
                title = { Text(dependencies.localization.removeBudget) },
                confirmButton = {
                    TextButton(
                        onClick = model::removeConfirm,
                        content = { Text(dependencies.localization.yes) },
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = model::removeCancel,
                        content = { Text(dependencies.localization.no) },
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
            overlineContent = { Text(dependencies.localization.budgetName) },
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
        id: String,
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
    ) {
        item(
            key = "button_$id",
        ) {
            ListItem(
                leadingContent = { Icon(icon = icon) },
                modifier = Modifier.clickable(onClick = onClick),
                headlineContent = { Text(title) },
                trailingContent = { Icon(Icons.Filled.ChevronRight) }
            )
        }
    }
}