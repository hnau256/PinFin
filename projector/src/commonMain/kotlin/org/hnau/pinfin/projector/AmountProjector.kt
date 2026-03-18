package org.hnau.pinfin.projector

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.TextInput
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.pinfin.model.AmountModel
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter


class AmountProjector(
    private val model: AmountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        shape: Shape = TextFieldDefaults.shape,
        imeAction: ImeAction = ImeAction.Unspecified,
        onImeAction: StateFlow<(KeyboardActionScope.() -> Unit)?> = null.toMutableStateFlowAsInitial(),
    ) {
        val currentOnImeAction by onImeAction.collectAsState()
        TextInput(
            modifier = modifier,
            value = model.input,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = imeAction,
            ),
            maxLines = 1,
            keyboardActions = currentOnImeAction
                ?.let { action -> KeyboardActions { action() } }
                ?: KeyboardActions(),
            label = { Text(dependencies.localization.amount) },
            isError = model.error.collectAsState().value,
            shape = shape,
        )
    }
}