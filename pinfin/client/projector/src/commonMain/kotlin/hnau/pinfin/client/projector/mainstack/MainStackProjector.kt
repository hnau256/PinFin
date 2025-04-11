package hnau.pinfin.client.projector.mainstack

import androidx.compose.runtime.Composable
import hnau.common.compose.projector.stack.Content
import hnau.common.compose.projector.stack.StackProjectorTail
import hnau.pinfin.client.model.mainstack.MainStackElementModel
import hnau.pinfin.client.model.mainstack.MainStackModel
import hnau.pinfin.client.projector.main.MainProjector
import hnau.pinfin.client.projector.transaction.TransactionProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class MainStackProjector(
    private val scope: CoroutineScope,
    private val model: MainStackModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun main(): MainProjector.Dependencies

        fun transaction(): TransactionProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Any?, MainStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is MainStackElementModel.Main -> MainStackElementProjector.Main(
                        MainProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.main(),
                        )
                    )

                    is MainStackElementModel.Transaction -> MainStackElementProjector.Transaction(
                        TransactionProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.transaction(),
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