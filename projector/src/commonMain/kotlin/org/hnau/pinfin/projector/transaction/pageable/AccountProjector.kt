package org.hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.pageable.AccountModel
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateMessages
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.ViewMode

class AccountProjector(
    private val model: AccountModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        AccountContent(
            info = model.account.collectAsState().value,
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            localization = dependencies.localization,
            viewMode = ViewMode.Full,
        )
    }

    companion object {

        @Pipe
        interface Dependencies {

            val localization: Localization

            fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies
        }

        @Composable
        fun chooseMessages(
            dependencies: Dependencies,
        ): ChooseOrCreateMessages = ChooseOrCreateMessages(
                createNew = dependencies.localization.createNewAccount,
                notFound = dependencies.localization.accountsNotFound,
                noVariants = dependencies.localization.thereAreNoAccounts,
            )

        fun createPage(
            model: ChooseOrCreateModel<AccountInfo>,
            dependencies: Dependencies
        ): ChooseOrCreateProjector<AccountInfo> = ChooseOrCreateProjector(
            model = model,
            dependencies = dependencies.chooseOrCreate(),
        ) { account, isSelected, onClick ->
            AccountContent(
                info = account,
                selected = isSelected.collectAsState().value,
                onClick = onClick,
                localization = dependencies.localization,
                viewMode = ViewMode.Full,
            )
        }
    }
}