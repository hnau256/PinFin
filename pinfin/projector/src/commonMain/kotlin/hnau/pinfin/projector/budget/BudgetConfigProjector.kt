package hnau.pinfin.projector.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.pinfin.model.budget.BudgetConfigModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.budgets_sync
import hnau.pinfin.projector.to_budgets_list
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class BudgetConfigProjector(
    scope: CoroutineScope,
    private val model: BudgetConfigModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
        ) {
            button(
                titleRes = Res.string.to_budgets_list,
                onClick = model::openBudgetsList,
            )
            button(
                titleRes = Res.string.budgets_sync,
                onClick = model::openSync,
            )
        }
    }

    private fun LazyListScope.button(
        titleRes: StringResource,
        onClick: () -> Unit,
    ) {
        item(
            key = "button_${titleRes.key}",
        ) {
            ListItem(
                modifier = Modifier.clickable(onClick = onClick),
                headlineContent = { Text(stringResource(titleRes)) },
            )
        }
    }
}