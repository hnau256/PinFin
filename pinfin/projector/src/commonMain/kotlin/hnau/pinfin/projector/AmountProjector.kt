package hnau.pinfin.projector

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.common.kotlin.coroutines.mapMutableState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.mapper.Mapper
import hnau.common.model.EditingString
import hnau.common.model.toEditingString
import hnau.common.projector.uikit.TextInput
import hnau.common.projector.uikit.table.Cell
import hnau.pinfin.model.AmountModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.amount
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class AmountProjector(
    private val scope: CoroutineScope,
    private val model: AmountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    private val input: MutableStateFlow<EditingString> = model
        .state
        .mapMutableState(
            scope = scope,
            mapper = Mapper(
                direct = { state ->
                    state
                        .input
                        .ifNull {
                            state
                                .amount
                                ?.let(dependencies.amountFormatter::format)
                                .ifNull { "" }
                                .toEditingString()
                        }
                },
                reverse = { input ->
                    AmountModel.State(
                        input = input,
                        amount = dependencies.amountFormatter.parse(input.text),
                    )
                }
            )
        )

    fun Content(
        weight: Float? = null,
        imeAction: ImeAction = ImeAction.Unspecified,
        onImeAction: StateFlow<(KeyboardActionScope.() -> Unit)?> = null.toMutableStateFlowAsInitial(),
    ): Cell = Cell {
        val currentOnImeAction by onImeAction.collectAsState()
        TextInput(
            modifier = weight
                ?.let { weight -> Modifier.weight(weight) }
                ?: Modifier,
            shape = shape,
            value = input,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Companion.Decimal,
                imeAction = imeAction,
            ),
            keyboardActions = currentOnImeAction
                ?.let { action -> KeyboardActions { action() } }
                ?: KeyboardActions(),
            placeholder = { Text(stringResource(Res.string.amount)) },
            isError = model.error.collectAsState().value,
        )
    }
}