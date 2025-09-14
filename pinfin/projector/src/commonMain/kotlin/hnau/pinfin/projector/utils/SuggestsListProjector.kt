package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.toNonEmptyListOrNull
import arrow.core.toOption
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.kotlin.coroutines.Stickable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.predeterminated
import hnau.common.kotlin.coroutines.stateFlow
import hnau.common.kotlin.coroutines.stick
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.Comment
import hnau.common.kotlin.coroutines.flatMapWithScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SuggestsListProjector(
    scope: CoroutineScope,
    suggests: StateFlow<List<Comment>>,
    inputIsFocused: StateFlow<Boolean>,
    private val onSelect: (Comment) -> Unit,
) {

    private val suggests: StateFlow<StateFlow<NonEmptyList<Comment>>?> = inputIsFocused
        .flatMapWithScope(scope) { focusScope, focused ->
            focused.foldBoolean(
                ifFalse = { null.toMutableStateFlowAsInitial() },
                ifTrue = {
                    suggests
                        .mapState(scope) { it.toNonEmptyListOrNull() }
                        .stick(focusScope) { _, suggestsOrNull ->
                            suggestsOrNull.foldNullable(
                                ifNull = { Stickable.predeterminated(null) },
                                ifNotNull = { suggests ->
                                    Stickable.stateFlow(
                                        initial = suggests,
                                        tryUseNext = NonEmptyList<Comment>?::toOption,
                                        createResult = ::identity,
                                    )
                                }
                            )
                        }
                }
            )
        }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        suggests
            .collectAsState()
            .value
            .NullableStateContent(
                modifier = modifier,
                transitionSpec = TransitionSpec.vertical(),
                label = "CommentSuggests"
            ) { suggestsFlow ->
                val suggests by suggestsFlow.collectAsState()
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(horizontal = Dimens.smallSeparation),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
                ) {
                    items(
                        items = suggests,
                        key = Comment::text,
                    ) { comment ->
                        SuggestionChip(
                            onClick = { onSelect(comment) },
                            label = { Text(comment.text) },
                        )
                    }
                }
            }
    }
}