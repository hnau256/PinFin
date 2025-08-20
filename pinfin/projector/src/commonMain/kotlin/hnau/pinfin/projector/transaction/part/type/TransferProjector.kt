package hnau.pinfin.projector.transaction.part.type

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction_old_2.part.type.TransferModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TransferProjector(
    scope: CoroutineScope,
    model: TransferModel,
    dependencies: Dependencies,
) : PartTypeProjector {

    @Pipe
    interface Dependencies

    @Composable
    override fun MainContent(
        modifier: Modifier,
    ) {
        TODO("Not yet implemented")
    }

    @Composable
    override fun AmountContent(
        modifier: Modifier,
    ) {
        TODO("Not yet implemented")
    }
}