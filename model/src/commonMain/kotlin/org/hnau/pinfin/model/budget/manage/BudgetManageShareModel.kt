@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.manage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.utils.ClipboardAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.ShareCode
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class BudgetManageShareModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val budgetRepository: BudgetRepository

        val clipboardAccessor: ClipboardAccessor
    }

    @Serializable
    data class Skeleton(
        val shareCodeIsOpened: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    private val code: StateFlow<String> = dependencies
        .budgetRepository
        .state
        .mapState(scope) { state ->
            ShareCode
                .create(
                    id = dependencies.id,
                    info = state.info,
                )
                .let(ShareCode.stringMapper.reverse)
        }

    sealed interface State {

        val ordinal: Int

        data class Closed(
            val openAndCopyCode: () -> Unit,
        ) : State {

            override val ordinal: Int
                get() = 0
        }

        data class Opened(
            val code: StateFlow<String>,
            val copyCode: () -> Unit,
        ) : State {

            override val ordinal: Int
                get() = 1
        }
    }

    val state: StateFlow<State> = skeleton
        .shareCodeIsOpened
        .mapState(scope) { shareCodeIsOpened ->
            shareCodeIsOpened.foldBoolean(
                ifFalse = {
                    State.Closed(
                        openAndCopyCode = {
                            skeleton.shareCodeIsOpened.value = true
                            copyCode()
                        }
                    )
                },
                ifTrue = {
                    State.Opened(
                        code = code,
                        copyCode = ::copyCode,
                    )
                }
            )
        }

    private fun copyCode() {
        dependencies
            .clipboardAccessor
            .copyToClipboard(code.value)
    }
}