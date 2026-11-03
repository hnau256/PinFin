package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.AccountChooseModel
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.BaseOrAdditionalValues
import org.hnau.pinfin.model.utils.budget.state.AccountIdWithInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection
import org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate.ChooseOrCreateMessages
import org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.chipInfo

class AccountChooseEditProjector(
    scope: CoroutineScope,
    private val model: AccountChooseModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies
    }

    private val delegate: ChooseOrCreateProjector<AccountIdWithInfo> = ChooseOrCreateProjector(
        scope = scope,
        model = model.choose,
        dependencies = dependencies.chooseOrCreate(),
        extractChipInfo = { it.value.chipInfo },
        messages = dependencies
            .localization
            .run {
                ChooseOrCreateMessages(
                    entityTypeName = category,
                    sections = BaseOrAdditionalValues(
                        base = ChooseOrCreateMessages.Section(
                            title = chooseAccountVariantsTitle,
                            noVariantsNoInput = chooseAccountVariantsEmptyNoInput,
                            noVariantsWithInput = chooseAccountVariantsEmptyInput,
                        ),
                        additional = ChooseOrCreateMessages.Section(
                            title = chooseAccountAdditionalVariantsTitle,
                            noVariantsNoInput = chooseAccountAdditionalVariantsEmptyNoInput,
                            noVariantsWithInput = chooseAccountAdditionalVariantsEmptyInput,
                        ),
                    )
                )
            },
    )

    val section: TransactionSection<Unit>
        get() = delegate.section

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        delegate.Content(
            modifier = modifier,
        )
    }
}
