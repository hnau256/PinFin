package hnau.common.app.model.stack

import hnau.common.kotlin.coroutines.mapReusable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

inline fun <S, M> StackModelElements(
    scope: CoroutineScope,
    skeletonsStack: StateFlow<NonEmptyStack<S>>,
    crossinline createModel: (CoroutineScope, S) -> M,
): StateFlow<NonEmptyStack<M>> = skeletonsStack
    .mapReusable(
        scope = scope,
    ) {skeletons ->
        skeletons.map {skeleton ->
            getOrPutItem(
                key = skeleton,
            ) {modelScope ->
                createModel(modelScope, skeleton)
            }
        }
    }