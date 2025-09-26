@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.accountstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.tryDropLast
import hnau.common.app.model.stack.withModels
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
) {

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

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<AccountStackElementModel.Skeleton, AccountStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = AccountStackElementModel.Skeleton::key,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
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
                onSelect = { icon ->
                    this@AccountStackModel.skeleton.icon.value = icon
                    this@AccountStackModel.skeleton.stack.tryDropLast()
                },
            )
        )
    }

    val stack: StateFlow<NonEmptyStack<AccountStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = AccountStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}