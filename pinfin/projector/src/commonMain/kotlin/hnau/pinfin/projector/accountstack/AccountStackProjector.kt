package hnau.pinfin.projector.accountstack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.pinfin.model.accountstack.AccountStackElementModel
import hnau.pinfin.model.accountstack.AccountStackModel
import hnau.pinfin.projector.IconProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class AccountStackProjector(
    private val scope: CoroutineScope,
    private val model: AccountStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun info(): AccountProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, AccountStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is AccountStackElementModel.Info -> AccountStackElementProjector.Info(
                        AccountProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.info(),
                        )
                    )

                    is AccountStackElementModel.Icon -> AccountStackElementProjector.Icon(
                        IconProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.icon(),
                        )
                    )
                }
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}