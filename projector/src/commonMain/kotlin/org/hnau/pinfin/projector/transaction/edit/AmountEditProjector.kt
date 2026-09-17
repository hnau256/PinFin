package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.expression.Expression
import org.hnau.pinfin.data.expression.fold
import org.hnau.pinfin.model.transaction.edit.AmountEditModel
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class AmountEditProjector(
    private val model: AmountEditModel,
    private val localization: Localization,
    private val amountFormatter: AmountFormatter,
    private val budgetRepository: BudgetRepository,
) {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val isFocused = model.navigateContext.isFocused.collectAsState().value
        val input = model.input.collectAsState().value
        val editable = model.amountEditable.collectAsState().value
        val currency = budgetRepository.state.collectAsState().value.info.currency

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
                SText(localization.amount)
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
                        text = resultText ?: localization.amount,
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
    ): String = amountFormatter.format(
        amount = KeyValue(
            key = AmountDirection.Credit,
            value = amount,
        ),
        alwaysShowSign = false,
    )
}
