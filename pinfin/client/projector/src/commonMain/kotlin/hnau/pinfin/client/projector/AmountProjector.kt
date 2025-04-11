package hnau.pinfin.client.projector

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import hnau.common.app.EditingString
import hnau.common.app.toEditingString
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.table.TableScope
import hnau.common.compose.uikit.table.cellShape
import hnau.common.kotlin.coroutines.mapMutableState
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.mapper.Mapper
import hnau.pinfin.client.model.AmountModel
import hnau.pinfin.client.projector.utils.AmountFormatter
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class AmountProjector(
    private val scope: CoroutineScope,
    private val model: AmountModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
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

    @Composable
    fun Content(
        scope: TableScope,
        modifier: Modifier = Modifier.Companion,
    ) {
        with(scope) {
            Cell {
                TextInput(
                    modifier = modifier,
                    shape = cellShape,
                    value = input,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Companion.Decimal,
                    ),
                    //TODO("ComposeForAndroid")
                    placeholder = { Text("QWERTY"/*stringResource(R.string.amount)*/) },
                    isError = model.error.collectAsState().value,
                )
            }
        }
    }
}