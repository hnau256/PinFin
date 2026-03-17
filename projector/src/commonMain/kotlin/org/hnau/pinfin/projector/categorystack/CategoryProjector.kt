package org.hnau.pinfin.projector.categorystack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.TopBarAction
import org.hnau.commons.app.projector.uikit.TopBarTitle
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.ifNull
import org.hnau.pinfin.model.categorystack.CategoryModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.BackButtonWidth
import org.hnau.pinfin.projector.utils.HueSlider
import org.hnau.pinfin.projector.utils.image


class CategoryProjector(
    scope: CoroutineScope,
    private val model: CategoryModel,
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
                    TopBarTitle { Text(dependencies.localization.categorySettings) }
                    SaveAction()
                }
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                item(
                    key = "title"
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        overlineContent = {
                            Text((dependencies.localization.name))
                        },
                        headlineContent = {
                            val focusRequester = remember { FocusRequester() }
                            TextInput(
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                value = model.title,
                                isError = !model.titleIsCorrect.collectAsState().value,
                            )
                        },
                    )
                }
                item(
                    key = "hue",
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        overlineContent = {
                            Text((dependencies.localization.hue))
                        },
                        headlineContent = {
                            HueSlider(
                                value = model.hue,
                                modifier = Modifier
                                    .fillMaxWidth(),
                            )
                        },
                    )
                }
                item(
                    key = "icon",
                ) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { model.chooseIcon() },
                        headlineContent = {
                            Text((dependencies.localization.icon))
                        },
                        trailingContent = {
                            Icon(
                                icon = model
                                    .icon
                                    .collectAsState()
                                    .value
                                    ?.image
                                    .ifNull { Icons.Filled.Image }
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun SaveAction() {
        val saveFlow by model.save.collectAsState()
        val save = saveFlow?.collectAsState()?.value
        val isSaving = saveFlow != null && save == null
        TopBarAction(
            onClick = save,
        ) {
            when (isSaving) {
                true -> CircularProgressIndicator()
                false -> Icon(Icons.Filled.Save)
            }
        }
    }
}