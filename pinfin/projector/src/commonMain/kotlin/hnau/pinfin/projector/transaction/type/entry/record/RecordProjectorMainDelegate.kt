package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import arrow.core.NonEmptyList
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.app.model.toEditingString
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.collectAsMutableState
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.AmountProjector
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class RecordProjectorMainDelegate(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val model: RecordModel,
) {

    @Pipe
    interface Dependencies {

        fun amount(): AmountProjector.Dependencies
    }

    private val amount = AmountProjector(
        scope = scope,
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    private val commentIsFocused: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    private val commentSuggests: StateFlow<StateFlow<NonEmptyList<Comment>>?> = commentIsFocused
        .flatMapState(scope) { commentIsFocused ->
            commentIsFocused.foldBoolean(
                ifTrue = { model.commentSuggests },
                ifFalse = { null.toMutableStateFlowAsInitial() },
            )
        }

    @Composable
    fun Content() {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var comment by model.comment.collectAsMutableState()
                /*TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.smallSeparation)
                        .onFocusChanged { state ->
                            commentIsFocused.value = state.isFocused
                        },
                    label = { Text(stringResource(Res.string.comment)) },
                )*/
                model
                    .openRemoveOverlap
                    .collectAsState()
                    .value
                    .NullableStateContent(
                        transitionSpec = TransitionSpec.horizontal(),
                    ) { openRemoveOverlap ->
                        IconButton(
                            onClick = openRemoveOverlap,
                        ) {
                            Icon(Icons.Filled.Clear)
                        }
                    }
            }
            commentSuggests
                .collectAsState()
                .value
                .NullableStateContent(
                    transitionSpec = TransitionSpec.vertical(),
                    modifier = Modifier.fillMaxWidth(),
                ) { suggestsFlow ->
                    val suggests by suggestsFlow.collectAsState()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = Dimens.smallSeparation),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        items(
                            items = suggests,
                            key = Comment::text,
                        ) { comment ->
                            SuggestionChip(
                                onClick = {
                                    model.comment.value = comment.text.toEditingString()
                                },
                                label = { Text(comment.text) },
                                shape = HnauShape(),
                            )
                        }
                    }
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                CategoryButton(
                    modifier = Modifier.weight(1f),
                    info = model.category.collectAsState().value,
                    onClick = model::openCategoryChooser,
                )
                amount.Content(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = Dimens.smallSeparation),
                )
            }
        }
    }
}