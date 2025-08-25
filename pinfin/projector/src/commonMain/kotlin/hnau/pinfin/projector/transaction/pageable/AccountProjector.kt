package hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction.pageable.AccountModel
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.accounts_not_found
import hnau.pinfin.projector.resources.create_new_account
import hnau.pinfin.projector.resources.there_are_no_accounts
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateMessages
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import hnau.pinfin.projector.utils.AccountButton
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class AccountProjector(
    scope: CoroutineScope,
    private val model: AccountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        AccountButton(
            info = model.account.collectAsState().value,
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
        )
    }

    companion object {

        val chooseMessages: ChooseOrCreateMessages
            @Composable
            get() = ChooseOrCreateMessages(
                createNew = stringResource(Res.string.create_new_account),
                notFound = stringResource(Res.string.accounts_not_found),
                noVariants = stringResource(Res.string.there_are_no_accounts),
            )

        fun createPage(
            scope: CoroutineScope,
            model: ChooseOrCreateModel<AccountInfo>,
            dependencies: ChooseOrCreateProjector.Dependencies,
        ): ChooseOrCreateProjector<AccountInfo> = ChooseOrCreateProjector(
            scope = scope,
            model = model,
            dependencies = dependencies,
        ) { account, isSelected, onClick ->
            AccountButton(
                info = account,
                selected = isSelected.collectAsState().value,
                onClick = onClick,
            )
        }
    }
}