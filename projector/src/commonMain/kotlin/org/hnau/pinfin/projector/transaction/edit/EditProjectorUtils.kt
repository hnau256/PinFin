package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STextField
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.ifFalse
import org.hnau.commons.kotlin.ifTrue
import org.hnau.pinfin.model.transaction.edit.ChooseOrCreateModel
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext

@Composable
internal fun EditTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    navigateContext: EditNavigateContext,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isFocused = navigateContext.isFocused.collectAsState().value
    LaunchedEffect(isFocused) {
        isFocused.foldBoolean(
            ifTrue = { focusRequester.requestFocus() },
            ifFalse = { focusManager.clearFocus() },
        )
    }
    STextField(
        value = value,
        onValueChanged = onValueChanged,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                focusState.isFocused.ifTrue {
                    navigateContext.requestFocus()
                }
            },
        keyboardOptions = keyboardOptions,
        onKeyboardAction = KeyboardActionHandler { navigateContext.goForward() },
    )
}

@Composable
internal fun ReadOnlyValue(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        SText(text = text)
    }
}

@Composable
internal fun <T> ChipsRow(
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
) {
    items
        .isEmpty()
        .ifFalse {
            val separation = LocalDistance.current.units.padding.along.small
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(separation),
                verticalArrangement = Arrangement.spacedBy(separation),
            ) {
                items.forEach { item ->
                    itemContent(item)
                }
            }
        }
}

@Composable
internal fun <T> ChooseOrCreateContent(
    model: ChooseOrCreateModel<T>,
    itemContent: @Composable (T) -> Unit,
) {
    val input = model.input.collectAsState().value
    val variants = model.variants.collectAsState().value
    Column(
        verticalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small),
    ) {
        variants.NullableStateContent(
            transitionSpec = TransitionSpec.remember(
                showAlignment = Alignment.BottomCenter,
            ),
        ) { variantsNotNull ->
            ChipsRow(items = variantsNotNull) { variant ->
                SPanel(
                    actionOrElseOrDisabled = ActionOrElse.instant(variant.onClickIfNotSelected),
                    isSelected = variant.onClickIfNotSelected == null,
                ) {
                    itemContent(variant.value)
                }
            }
        }
        EditTextField(
            value = input,
            onValueChanged = { model.input.value = it },
            navigateContext = model.navigateContext,
        )
    }
}
