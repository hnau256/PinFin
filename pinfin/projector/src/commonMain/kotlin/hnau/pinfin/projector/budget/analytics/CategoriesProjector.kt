package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel
import hnau.pinfin.projector.utils.category.CategoryContent
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class CategoriesProjector(
    scope: CoroutineScope,
    private val model: CategoriesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val categories by model.categories.collectAsState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalDisplayPadding(),
            contentPadding = contentPadding,
        ) {
            items(
                items = categories,
                key = { it.id.id },
            ) {
                CategoryContent(
                    info = it,
                )
            }
        }
    }
}