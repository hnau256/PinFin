@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.accountstack

import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.tryDropLast
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.IconModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.icons.IconVariant
import org.hnau.commons.gen.pipe.annotations.Pipe
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
        val stack: MutableStateFlow<NonEmptyStack<AccountStackElementSkeleton>> = NonEmptyStack(
            tail = ElementSkeleton.info(
                info = info,
            )
        ).toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        fun info(): AccountModel.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = AccountModel::class,
                identifier = "info",
            ),
            Variant(
                type = IconModel::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "AccountStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = AccountModel.Skeleton::class,
                identifier = "info",
            ),
            Variant(
                type = IconModel.Skeleton::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "AccountStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<AccountStackElementSkeleton, AccountStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = AccountStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: AccountStackElementSkeleton,
    ): AccountStackElementModel = skeleton.fold(
        ifInfo = { infoSkeleton ->
            Element.info(
                scope = modelScope,
                skeleton = infoSkeleton,
                dependencies = dependencies.info(),
                onReady = onReady,
                info = this@AccountStackModel.skeleton.info,
                icon = this@AccountStackModel.skeleton.icon,
                chooseIcon = {
                    this@AccountStackModel.skeleton.stack.push(
                        ElementSkeleton.icon()
                    )
                }
            )
        },
        ifIcon = { iconSkeleton ->
            Element.icon(
                scope = modelScope,
                skeleton = iconSkeleton,
                selected = this@AccountStackModel.skeleton.icon.value,
                onSelect = { icon ->
                    this@AccountStackModel.skeleton.icon.value = icon
                    this@AccountStackModel.skeleton.stack.tryDropLast()
                },
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<AccountStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = AccountStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}