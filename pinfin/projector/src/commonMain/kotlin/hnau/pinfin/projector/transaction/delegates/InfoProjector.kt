package hnau.pinfin.projector.transaction.delegates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.transaction.pageable.CommentProjector
import hnau.pinfin.projector.transaction.pageable.DateProjector
import hnau.pinfin.projector.transaction.pageable.TimeProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class InfoProjector(
    scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val type: TypeProjector

        fun date(): DateProjector.Dependencies

        fun time(): TimeProjector.Dependencies
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
                Column(
                    modifier = Modifier
                        .padding(Dimens.smallSeparation),
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

                    Row(
                        modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        comment.Content(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                        )
                        type.AmountContent(
                            modifier = Modifier.fillMaxHeight(),
                        )
                    }

                    type.MainContent(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}