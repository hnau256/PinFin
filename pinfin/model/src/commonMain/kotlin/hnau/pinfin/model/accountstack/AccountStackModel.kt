@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.accountstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.app.model.stack.tryDropLast
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.IconModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AccountStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val info: AccountInfo,
        val icon: MutableStateFlow<IconVariant?> = info.icon.toMutableStateFlowAsInitial(),
        val stack: MutableStateFlow<NonEmptyStack<AccountStackElementModel.Skeleton>> = NonEmptyStack(
            tail = AccountStackElementModel.Skeleton.Info(
                skeleton = AccountModel.Skeleton(
                    info = info,
                )
            )
        ).toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        fun info(): AccountModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<AccountStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            getKey = AccountStackElementModel.Skeleton::key,
            skeletonsStack = stack,
        ) { modelScope, skeleton ->
            createModel(
                modelScope = modelScope,
                skeleton = skeleton,
            )
        }
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: AccountStackElementModel.Skeleton,
    ): AccountStackElementModel = when (skeleton) {
        is AccountStackElementModel.Skeleton.Info -> AccountStackElementModel.Info(
            AccountModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.info(),
                onReady = onReady,
                info = this@AccountStackModel.skeleton.info,
                icon = this@AccountStackModel.skeleton.icon,
                chooseIcon = {
                    this@AccountStackModel.skeleton.stack.push(
                        AccountStackElementModel.Skeleton.Icon()
                    )
                }
            )
        )

        is AccountStackElementModel.Skeleton.Icon -> AccountStackElementModel.Icon(
            IconModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                selected = this@AccountStackModel.skeleton.icon.value,
                onSelect = {icon ->
                    this@AccountStackModel.skeleton.icon.value = icon
                    this@AccountStackModel.skeleton.stack.tryDropLast()
                },
            )
        )
    }

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}