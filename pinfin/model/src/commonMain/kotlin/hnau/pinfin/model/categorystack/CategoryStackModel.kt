@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.categorystack

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.tryDropLast
import hnau.common.app.model.stack.withModels
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.IconModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CategoryStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onReady: () -> Unit,
) {

    @Serializable
    data class Skeleton(
        val info: CategoryInfo,
        val icon: MutableStateFlow<IconVariant?> = info.icon.toMutableStateFlowAsInitial(),
        val stack: MutableStateFlow<NonEmptyStack<CategoryStackElementSkeleton>> = NonEmptyStack(
            tail = ElementSkeleton.info(
                info = info,
            )
        ).toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        fun info(): CategoryModel.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = CategoryModel::class,
                identifier = "info",
            ),
            Variant(
                type = IconModel::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "CategoryStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = CategoryModel.Skeleton::class,
                identifier = "info",
            ),
            Variant(
                type = IconModel.Skeleton::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "CategoryStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<CategoryStackElementSkeleton, CategoryStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = CategoryStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: CategoryStackElementSkeleton,
    ): CategoryStackElementModel = skeleton.fold(
        ifInfo = { infoSkeleton ->
            Element.info(
                scope = modelScope,
                skeleton = infoSkeleton,
                dependencies = dependencies.info(),
                onReady = onReady,
                info = this@CategoryStackModel.skeleton.info,
                icon = this@CategoryStackModel.skeleton.icon,
                chooseIcon = {
                    this@CategoryStackModel.skeleton.stack.push(
                        ElementSkeleton.icon()
                    )
                }
            )
        },
        ifIcon = { iconSkeleton ->
            Element.icon(
                scope = modelScope,
                skeleton = iconSkeleton,
                selected = this@CategoryStackModel.skeleton.icon.value,
                onSelect = { icon ->
                    this@CategoryStackModel.skeleton.icon.value = icon
                    this@CategoryStackModel.skeleton.stack.tryDropLast()
                },
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<CategoryStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = CategoryStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}