@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.categorystack

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
import hnau.pinfin.model.categorystack.CategoryModel
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
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val info: CategoryInfo,
        val icon: MutableStateFlow<IconVariant?> = info.icon.toMutableStateFlowAsInitial(),
        val stack: MutableStateFlow<NonEmptyStack<CategoryStackElementModel.Skeleton>> = NonEmptyStack(
            tail = CategoryStackElementModel.Skeleton.Info(
                skeleton = CategoryModel.Skeleton(
                    info = info,
                )
            )
        ).toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        fun info(): CategoryModel.Dependencies

        fun icon(): IconModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<CategoryStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            getKey = CategoryStackElementModel.Skeleton::key,
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
        skeleton: CategoryStackElementModel.Skeleton,
    ): CategoryStackElementModel = when (skeleton) {
        is CategoryStackElementModel.Skeleton.Info -> CategoryStackElementModel.Info(
            CategoryModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.info(),
                onReady = onReady,
                info = this@CategoryStackModel.skeleton.info,
                icon = this@CategoryStackModel.skeleton.icon,
                chooseIcon = {
                    this@CategoryStackModel.skeleton.stack.push(
                        CategoryStackElementModel.Skeleton.Icon()
                    )
                }
            )
        )

        is CategoryStackElementModel.Skeleton.Icon -> CategoryStackElementModel.Icon(
            IconModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.icon(),
                selected = this@CategoryStackModel.skeleton.icon.value,
                onSelect = {icon ->
                    this@CategoryStackModel.skeleton.icon.value = icon
                    this@CategoryStackModel.skeleton.stack.tryDropLast()
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