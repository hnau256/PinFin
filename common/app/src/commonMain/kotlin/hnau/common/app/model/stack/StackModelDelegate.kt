package hnau.common.app.model.stack

import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.ifNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

inline fun <S, M> StackModelElements(
    scope: CoroutineScope,
    skeletonsStack: StateFlow<NonEmptyStack<S>>,
    crossinline createModel: (CoroutineScope, S) -> M,
): StateFlow<NonEmptyStack<M>> = skeletonsStack
    .runningFoldState(
        scope = scope,
        createInitial = { skeletons ->
            skeletons.toModels(
                scope = scope,
                createModel = createModel,
            )
        },
        operation = { previousModels, skeletons ->
            skeletons.toModels(
                scope = scope,
                previousModels = previousModels,
                createModel = createModel,
            )
        },
    )
    .mapState(scope) { modelHoldersStack ->
        modelHoldersStack.map(ModelHolder<S, M>::model)
    }

@PublishedApi
internal data class ModelHolder<S, M>(
    val skeleton: S,
    val model: M,
    val cancel: () -> Unit,
)

@PublishedApi
internal inline fun <S, M> NonEmptyStack<S>.toModels(
    scope: CoroutineScope,
    previousModels: NonEmptyStack<ModelHolder<S, M>>? = null,
    createModel: (CoroutineScope, S) -> M,
): NonEmptyStack<ModelHolder<S, M>> {
    val modelsCache: MutableMap<S, ModelHolder<S, M>> = previousModels
        ?.all
        ?.associateBy(ModelHolder<S, M>::skeleton)
        .ifNull { emptyMap() }
        .toMutableMap()
    val result = map { skeleton ->
        modelsCache
            .remove(skeleton)
            .ifNull {
                val modelScope = scope.createChild()
                ModelHolder(
                    skeleton = skeleton,
                    model = createModel(modelScope, skeleton),
                    cancel = { modelScope.cancel() },
                )
            }
    }
    modelsCache.values.forEach { it.cancel() }
    return result
}
