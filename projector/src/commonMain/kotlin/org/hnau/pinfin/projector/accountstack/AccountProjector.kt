package org.hnau.pinfin.projector.accountstack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.padding.LocalContentPadding
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.app.projector.utils.collectAsMutableAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.ifNull
import org.hnau.pinfin.model.accountstack.AccountModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.HueSlider
import org.hnau.pinfin.projector.utils.image

class AccountProjector(
    scope: CoroutineScope,
    private val model: AccountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {


        val localization: Localization
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText(dependencies.localization.accountSettings) },
            actions = {
                model
                    .save
                    .collectAsState()
                    .value
                    ?.let { save ->
                        Action(
                            actionOrElseOrDisabled = save.collectAsState().value,
                            titleOrIcon = TitleOrIcon.Icon(
                                Drawable.Vector(Icons.Filled.Save)
                            )
                        )
                    }
            }
        ) {
            val contentPadding = LocalContentPadding.current
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
                            Text(dependencies.localization.hue)
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
                item(
                    key = "hide_if_amount_if_zero",
                ) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { model.chooseIcon() },
                        headlineContent = {
                            Text(dependencies.localization.hideIfAmountIsZero)
                        },
                        trailingContent = {
                            var hideIfAccountIsZero by model.hideIfAmountIsZero.collectAsMutableAccessor()
                            Switch(
                                checked = hideIfAccountIsZero,
                                onCheckedChange = { hideIfAccountIsZero = it },
                            )
                        }
                    )
                }
            }
        }
    }
}