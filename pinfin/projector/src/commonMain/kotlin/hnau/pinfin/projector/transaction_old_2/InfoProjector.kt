package hnau.pinfin.projector.transaction_old_2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.model.transaction_old_2.TransactionModel
import hnau.pinfin.projector.transaction_old_2.part.CommentProjector
import hnau.pinfin.projector.transaction_old_2.part.DateProjector
import hnau.pinfin.projector.transaction_old_2.part.TimeProjector
import hnau.pinfin.projector.transaction_old_2.part.TypeProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class InfoProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val type: TypeProjector

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

    private val type: TypeProjector
        get() = dependencies.type

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
                        type.MainContent(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    type.AmountContent()
                }
            }
        }
    }
}