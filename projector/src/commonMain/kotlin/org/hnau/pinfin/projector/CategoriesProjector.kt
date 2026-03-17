package org.hnau.pinfin.projector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.ErrorPanel
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.CategoriesModel
import org.hnau.pinfin.projector.utils.BackButtonWidth
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.ViewMode


class CategoriesProjector(
    private val model: CategoriesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        val localization: Localization
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        FullScreen(
            contentPadding = contentPadding,
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle {
                        Text((dependencies.localization.categories))
                    }
                }
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
                                    text = dependencies.localization.thereAreNoCategories,
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
                                            localization = dependencies.localization,
                                            viewMode = ViewMode.Full,
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