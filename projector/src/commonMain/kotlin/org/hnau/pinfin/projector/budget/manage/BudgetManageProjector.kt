package org.hnau.pinfin.projector.budget.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.projector.Localization


class BudgetManageProjector(
    private val scope: CoroutineScope,
    private val model: BudgetManageModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun remove(): BudgetManageRemoveProjector.Dependencies
    }

    private val remove = BudgetManageRemoveProjector(
        scope = scope,
        model = model.remove,
        dependencies = dependencies.remove(),
    )


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
                onClick = remove::onRemoveClick,
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
        remove.Content()
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