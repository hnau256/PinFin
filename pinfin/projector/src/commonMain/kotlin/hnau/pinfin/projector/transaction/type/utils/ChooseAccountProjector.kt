package hnau.pinfin.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.transaction.type.utils.ChooseAccountModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.accounts_not_found
import hnau.pinfin.projector.create_new_account
import hnau.pinfin.projector.there_are_no_accounts
import hnau.pinfin.projector.utils.account.AccountButton
import hnau.pinfin.projector.utils.choose.ChooseMessages
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
                messages = ChooseMessages(
                    createNew = stringResource(Res.string.create_new_account),
                    notFound = stringResource(Res.string.accounts_not_found),
                    noVariants = stringResource(Res.string.there_are_no_accounts),
                ),
            ) { accountInfo, selected, onClick ->
                AccountButton(
                    info = accountInfo,
                    onClick = onClick,
                    selected = selected,
                )
            }
    }
}