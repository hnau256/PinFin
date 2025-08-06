package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.transaction.part.CommentProjector
import hnau.pinfin.projector.transaction.part.DateProjector
import hnau.pinfin.projector.transaction.part.TimeProjector
import hnau.pinfin.projector.utils.Tabs
import hnau.pinfin.projector.utils.title
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope

class InfoProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun date(): DateProjector.Dependencies

        fun time(): TimeProjector.Dependencies

        fun comment(): CommentProjector.Dependencies
    }

    private val date = DateProjector(
        scope = scope,
        dependencies = dependencies.date(),
        model = model.date,
    )

    private val time = TimeProjector(
        scope = scope,
        dependencies = dependencies.time(),
        model = model.time,
    )

    private val comment = CommentProjector(
        scope = scope,
        dependencies = dependencies.comment(),
        model = model.comment,
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .padding(contentPadding)
                .horizontalDisplayPadding()
                .verticalDisplayPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            var type by remember { mutableStateOf(TransactionType.Entry) }
            Tabs(
                items = TransactionType.entries.toImmutableList(),
                selected = type,
                onSelectedChanged = { type = it },
            ) {
                Text(it.title)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(Dimens.smallSeparation)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            date.Content(
                                modifier = Modifier.weight(1f),
                            )
                            time.Content(
                                modifier = Modifier.weight(1f),
                            )
                        }
                        comment.Content(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    //TODO type amount
                }
            }
        }
    }
}