package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.copy
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction.pageable.AmountModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.transaction.utils.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.UIConstants
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AmountProjector(
    private val model: AmountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    class Page(
        scope: CoroutineScope,
        private val model: AmountModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun delegate(): AmountProjector.Dependencies
        }

        private val delegate: AmountProjector = AmountProjector(
            scope = scope,
            dependencies = dependencies.delegate(),
            model = model.delegate,
        )

        private val goForward: StateFlow<KeyboardActionScope.() -> Unit> =
            MutableStateFlow { model.goForward() }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            Column(
                modifier = modifier.horizontalDisplayPadding(),
            ) {
                val focusRequester = remember { FocusRequester() }
                delegate.Content(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(contentPadding.copy(bottom = 0.dp)),
                    imeAction = ImeAction.Next,
                    onImeAction = goForward,
                    shape = MaterialTheme.shapes.medium,
                )
                LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
            }
        }
    }

    @Composable
    fun InnerContent(
        modifier: Modifier = Modifier,
    ) {
        model
            .amount
            .collectAsState()
            .value
            .foldNullable(
                ifNull = {
                    Icon(
                        modifier = modifier,
                        icon = UIConstants.absentValueIcon,
                        tint = UIConstants.absentValueColor,
                    )
                },
                ifNotNull = { amount ->
                    Text(
                        modifier = modifier,
                        text = dependencies.amountFormatter.format(
                            amount = amount,
                            alwaysShowSign = false,
                            alwaysShowCents = false,
                        ),
                        maxLines = 1,
                        color =MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            InnerContent()
        }
    }
}