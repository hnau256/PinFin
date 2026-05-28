package org.hnau.pinfin.projector.budget.manage

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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import org.hnau.commons.app.projector.uikit.AlertDialogContent
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.onClick
import org.hnau.commons.app.projector.uikit.progressindicator.InProgress
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifFalse
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.projector.Localization


class BudgetManageProjector(
    private val model: BudgetManageModel,
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
                top = TopBarDefaults.height + TopBarDefaults.separationTop,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            button(
                id = "CreateBudget",
                title = dependencies.localization.toBudgetsList,
                onClick = model::openBudgetsList,
                icon = Icons.AutoMirrored.Filled.MenuOpen,
            )
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
                id = "Settings",
                title = dependencies.localization.settings,
                onClick = model::openSettings,
                icon = Icons.Filled.Settings,
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