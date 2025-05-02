package hnau.pinfin.projector.budget.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import arrow.core.Either
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.utils.Icon
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.budget.config.BudgetConfigModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.budget_name
import hnau.pinfin.projector.to_budgets_list
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class BudgetConfigProjector(
    scope: CoroutineScope,
    private val model: BudgetConfigModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun editName(): BudgetEditNameProjector.Dependencies
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.Companion.fillMaxSize(),
        ) {
            button(
                titleRes = Res.string.to_budgets_list,
                onClick = model::openBudgetsList,
                icon = { Icons.AutoMirrored.Filled.List },
            )
            name()
        }
    }

    private val nameOrEdit: StateFlow<Either<Pair<String, () -> Unit>, BudgetEditNameProjector>> =
        model
            .nameOrEdit
            .mapWithScope(scope) { stateScope, nameOrEdit ->
                nameOrEdit.map { editModel ->
                    BudgetEditNameProjector(
                        scope = stateScope,
                        model = editModel,
                        dependencies = dependencies.editName(),
                    )
                }
            }

    private fun LazyListScope.name() {
        item(
            key = "name",
        ) {
            nameOrEdit
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = TransitionSpec.vertical(),
                    label = "BudgetNameOrEdit",
                    contentKey = { it.isLeft() },
                ) { nameOrEdit ->
                    nameOrEdit.fold(
                        ifLeft = { (name, edit) ->
                            ListItem(
                                overlineContent = { Text(stringResource(Res.string.budget_name)) },
                                headlineContent = { Text(name) },
                                trailingContent = {
                                    IconButton(
                                        onClick = edit,
                                    ) {
                                        Icon { Icons.Filled.Edit }
                                    }
                                },
                                leadingContent = { Icon { Icons.Filled.Badge } },
                            )
                        },
                        ifRight = { editNameProjector ->
                            editNameProjector.Content()
                        },
                    )
                }
        }
    }

    private fun LazyListScope.button(
        icon: () -> ImageVector,
        titleRes: StringResource,
        onClick: () -> Unit,
    ) {
        item(
            key = "button_${titleRes.key}",
        ) {
            ListItem(
                leadingContent = { Icon(chooseIcon = icon) },
                modifier = Modifier.Companion.clickable(onClick = onClick),
                headlineContent = { Text(stringResource(titleRes)) },
            )
        }
    }
}