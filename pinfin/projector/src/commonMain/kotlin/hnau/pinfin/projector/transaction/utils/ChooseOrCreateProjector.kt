package hnau.pinfin.projector.transaction.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.uikit.row.ChipsFlowRow
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.collectAsTextFieldValueMutableAccessor
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.search_create
import hnau.pinfin.projector.utils.LabelDefaults
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class ChooseOrCreateProjector<T : Comparable<T>>(
    scope: CoroutineScope,
    private val model: ChooseOrCreateModel<T>,
    dependencies: Dependencies,
    private val itemContent: @Composable (
        value: T,
        isSelected: StateFlow<Boolean>,
        onClick: () -> Unit,
    ) -> Unit,
) {

    @Composable
    private fun ChooseOrCreateModel.State.Item<T>.Content() {
        itemContent(value, isSelected, onClick)
    }

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        messages: ChooseOrCreateMessages,
        modifier: Modifier = Modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            modifier = modifier,
        ) {
            Input(
                modifier = Modifier
                    .fillMaxWidth(),
            )
            State(
                messages = messages,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }

    @Composable
    fun State(
        messages: ChooseOrCreateMessages,
        modifier: Modifier = Modifier,
    ) {
        val state by model.state.collectAsState()
        Column(
            modifier = modifier
                .horizontalDisplayPadding(),
        ) {
            Filtered(
                messages = messages,
                modifier = Modifier
                    .fillMaxWidth(),
                filtered = state.filtered,
            )
            New(
                messages = messages,
                modifier = Modifier
                    .fillMaxWidth(),
                newOrNull = state.new,
            )
        }
    }

    @Composable
    private fun Filtered(
        messages: ChooseOrCreateMessages,
        filtered: ChooseOrCreateModel.State.Filtered<T>,
        modifier: Modifier = Modifier,
    ) {
        filtered
            .StateContent(
                modifier = modifier,
                label = "ChooseOrCreateFiltered",
                contentKey = { filteredLocal ->
                    when (filteredLocal) {
                        ChooseOrCreateModel.State.Filtered.NothingToFilter -> 0
                        is ChooseOrCreateModel.State.Filtered.Items<T> -> 1
                        ChooseOrCreateModel.State.Filtered.AllAreExcluded -> 2
                    }
                },
                transitionSpec = TransitionSpec.vertical(),
            ) { filteredLocal ->
                when (filteredLocal) {
                    ChooseOrCreateModel.State.Filtered.NothingToFilter -> Message(
                        message = messages.noVariants,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    is ChooseOrCreateModel.State.Filtered.Items<T> -> ChipsFlowRow(
                        all = filteredLocal.items,
                    ) { item -> item.Content() }

                    ChooseOrCreateModel.State.Filtered.AllAreExcluded -> Message(
                        message = messages.notFound,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
    }

    @Composable
    private fun Message(
        message: String,
        color: Color,
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(
                    vertical = Dimens.smallSeparation,
                ),
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }

    @Composable
    private fun New(
        messages: ChooseOrCreateMessages,
        newOrNull: ChooseOrCreateModel.State.Item<T>?,
        modifier: Modifier = Modifier,
    ) {
        newOrNull
            .NullableStateContent(
                modifier = modifier,
                transitionSpec = TransitionSpec.vertical(),
            ) { new ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    modifier = Modifier.padding(
                        vertical = Dimens.smallSeparation,
                    ),
                ) {
                    Text(
                        text = messages.createNew,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    new.Content()
                }
            }
    }

    @Composable
    private fun Input(
        modifier: Modifier = Modifier,
    ) {
        val focusRequester = remember { FocusRequester() }
        var query by model.query.collectAsTextFieldValueMutableAccessor()
        OutlinedTextField(
            leadingIcon = { Icon(Icons.Default.Search) },
            colors = PartDefaults.outlinedTextFieldColors,
            shape = LabelDefaults.shape,
            modifier = modifier
                .focusRequester(focusRequester)
                .horizontalDisplayPadding(),
            value = query,
            onValueChange = { query = it },
            maxLines = 1,
            placeholder = { Text(stringResource(Res.string.search_create)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            )
        )
        val state by model.state.collectAsState()
        val requestFocus = remember(state) {
            when (state.filtered) {
                is ChooseOrCreateModel.State.Filtered.Items<T> -> false
                ChooseOrCreateModel.State.Filtered.AllAreExcluded,
                ChooseOrCreateModel.State.Filtered.NothingToFilter -> true
            }
        }
        LaunchedEffect(focusRequester, requestFocus) {
            if (requestFocus) {
                focusRequester.requestFocus()
            }
        }
    }
}