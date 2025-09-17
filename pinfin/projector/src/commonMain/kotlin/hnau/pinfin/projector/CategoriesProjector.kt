package hnau.pinfin.projector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.model.goback.GlobalGoBackHandler
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.ErrorPanel
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.NavigationIcon
import hnau.pinfin.model.CategoriesModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.categories
import hnau.pinfin.projector.resources.there_are_no_categories
import hnau.pinfin.projector.utils.CategoryContent
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class CategoriesProjector(
    scope: CoroutineScope,
    private val model: CategoriesModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler =
        dependencies.globalGoBackHandler.resolve(scope)


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.categories)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
            },
        ) { contentPadding ->
            model
                .categories
                .collectAsState()
                .value
                .NullableStateContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = TransitionSpec.crossfade(),
                    nullContent = {
                        ErrorPanel(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            title = {
                                Text(
                                    text = stringResource(Res.string.there_are_no_categories)
                                )
                            }
                        )
                    },
                    anyContent = { categories ->
                        LazyColumn(
                            contentPadding = contentPadding,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = categories,
                                key = { it.info.id.id },
                            ) { category ->
                                ListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = category.onClick),
                                    headlineContent = {
                                        CategoryContent(
                                            info = category.info,
                                        )
                                    },
                                    trailingContent = { Icon(Icons.Filled.ChevronRight) },
                                )

                            }
                        }
                    }
                )
        }
    }
}