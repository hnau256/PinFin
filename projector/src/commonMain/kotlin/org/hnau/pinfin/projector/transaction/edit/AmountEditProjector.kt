package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.expression.fold
import org.hnau.pinfin.model.transaction.edit.AmountEditModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class AmountEditProjector(
    private val model: AmountEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val amountFormatter: AmountFormatter
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val isFocused by model.navigateContext.isFocused.collectAsState()
        val input by model.input.collectAsState()
        val editable by model.amountEditable.collectAsState()
        val currency by model.currency.collectAsState()

        val expression = editable.fold(
            ifIncorrect = { null },
            ifValue = { value, _ -> value },
        )
        val resultText = expression
            ?.toAmount(currency.scale)
            ?.let(::formatResult)
        val hasOperations = expression
            ?.expression
            ?.fold(
                ifBinaryOperation = { _, _, _ -> true },
                ifUnaryOperation = { _, _ -> true },
                ifValue = { false },
            )
            ?: false

        SItem(
            modifier = modifier,
            topAccessory = {
                SText(dependencies.localization.amount)
            },
            bottomAccessory = resultText
                ?.takeIf { isFocused && hasOperations }
                ?.let { text ->
                    { SText(text) }
                },
        ) {
            isFocused.BooleanStateContent(
                transitionSpec = TransitionSpec.rememberCrossfade(),
                falseContent = {
                    ReadOnlyValue(
                        text = resultText ?: dependencies.localization.amount,
                        onClick = model.navigateContext.requestFocus,
                    )
                },
                trueContent = {
                    EditTextField(
                        value = input,
                        onValueChanged = { model.input.value = it },
                        navigateContext = model.navigateContext,
                    )
                },
            )
        }
    }

    private fun formatResult(
        amount: Amount,
    ): String = dependencies.amountFormatter.format(
        amount = KeyValue(
            key = AmountDirection.Credit,
            value = amount,
        ),
        alwaysShowSign = false,
    )
}
