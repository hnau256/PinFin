package hnau.pinfin.client.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.client.model.transaction.type.utils.ChooseAccountModel
import hnau.pinfin.client.projector.utils.account.AccountButton
import hnau.pinfin.client.projector.utils.choose.Content
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class ChooseAccountProjector(
    private val scope: CoroutineScope,
    private val model: ChooseAccountModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        model
            .state
            .Content { accountInfo, selected, onClick ->
                AccountButton(
                    info = accountInfo,
                    onClick = onClick,
                    selected = selected,
                )
            }
    }
}