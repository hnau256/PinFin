package hnau.pinfin.client.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.client.data.budget.AccountInfoResolver
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
    interface Dependencies {

        val accountInfoResolver: AccountInfoResolver
    }

    @Composable
    fun Content() {
        model
            .state
            .Content { accountId, selected, onClick ->
                AccountButton(
                    id = accountId,
                    onClick = onClick,
                    selected = selected,
                    infoResolver = dependencies.accountInfoResolver,
                )
            }
    }
}