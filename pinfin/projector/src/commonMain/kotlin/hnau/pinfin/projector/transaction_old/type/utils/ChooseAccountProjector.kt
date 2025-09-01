package hnau.pinfin.projector.transaction_old.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.transaction_old.type.utils.ChooseAccountModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.accounts_not_found
import hnau.pinfin.projector.resources.create_new_account
import hnau.pinfin.projector.resources.there_are_no_accounts
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateMessages
import hnau.pinfin.projector.utils.AccountContent
import hnau.pinfin.projector.utils.choose.Content
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class ChooseAccountProjector(
    private val scope: CoroutineScope,
    private val model: ChooseAccountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        model
            .state
            .Content(
                messages = ChooseOrCreateMessages(
                    createNew = stringResource(Res.string.create_new_account),
                    notFound = stringResource(Res.string.accounts_not_found),
                    noVariants = stringResource(Res.string.there_are_no_accounts),
                ),
            ) { accountInfo, selected, onClick ->
                AccountContent(
                    info = accountInfo,
                    onClick = onClick,
                    selected = selected,
                )
            }
    }
}