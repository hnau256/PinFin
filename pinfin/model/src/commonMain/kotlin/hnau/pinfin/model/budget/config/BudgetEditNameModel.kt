@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.config

import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetEditNameModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onDone: () -> Unit,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val name: MutableStateFlow<EditingString>,
    ) {

        constructor(
            info: BudgetInfo,
        ) : this(
            name = info
                .title
                .let { title ->
                    EditingString(
                        text = title,
                        selection = IntRange(0, title.length),
                    )
                }
                .toMutableStateFlowAsInitial(),
        )
    }

    val name: MutableStateFlow<EditingString>
        get() = skeleton.name

    val save: StateFlow<(() -> Unit)?> = actionOrNullIfExecuting(
        scope = scope,
        action = {
            dependencies.budgetRepository.config(
                config = BudgetConfig(
                    title = skeleton.name.value.text.trim()
                )
            )
            onDone()
        }
    )

    override val goBackHandler: GoBackHandler =
        onDone.toMutableStateFlowAsInitial()
}