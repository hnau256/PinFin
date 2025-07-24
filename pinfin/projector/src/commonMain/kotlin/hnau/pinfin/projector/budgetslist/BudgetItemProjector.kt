package hnau.pinfin.projector.budgetslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import hnau.common.app.projector.uikit.progressindicator.InProgress
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.pinfin.model.budgetslist.item.BudgetItemModel
import hnau.pinfin.projector.utils.BidgetInfoContent
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class BudgetItemProjector(
    scope: CoroutineScope,
    private val model: BudgetItemModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Table(
                orientation = TableOrientation.Vertical,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Cell(
                    isLast = true,
                ) { modifier ->
                    Row(
                        modifier = modifier
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable(onClick = model::open)
                            .padding(
                                horizontal = Dimens.separation,
                                vertical = Dimens.smallSeparation,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        BidgetInfoContent(
                            info = model.info.collectAsState().value,
                        )
                    }
                }
            }
            InProgress(
                inProgress = model.inProgress,
                fillMaxSize = false,
            )
        }
    }
}