package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.AccountChooseModel
import org.hnau.pinfin.projector.Localization

class AccountChooseEditProjector(
    private val model: AccountChooseModel,
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
        val isFocused by model.choose.navigateContext.isFocused.collectAsState()
        val account by model.accountEditable.collectAsState()
        val accountInfo = account.fold(
            ifIncorrect = { null },
            ifValue = { value, _ -> value.value },
        )

        SItem(
            modifier = modifier,
            topAccessory = {
                SText(dependencies.localization.account)
            },
        ) {
            isFocused.BooleanStateContent(
                transitionSpec = TransitionSpec.rememberCrossfade(),
                falseContent = {
                    ReadOnlyValue(
                        text = accountInfo?.title ?: dependencies.localization.account,
                        onClick = model.choose.navigateContext.requestFocus,
                    )
                },
                trueContent = {
                    ChooseOrCreateContent(model = model.choose) { idWithAccount ->
                        SText(idWithAccount.value.title)
                    }
                },
            )
        }
    }
}
