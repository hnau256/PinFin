package hnau.common.compose.projector.stack

import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.coroutines.runningFoldState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

data class StackProjectorTail<K, P>(
    val projector: P,
    val isNew: Boolean?,
    val key: K,
)

@PublishedApi
internal data class TailInfo<K, P>(
    val cancel: () -> Unit,
    val stackKeys: Set<K>,
    val tail: StackProjectorTail<K, P>,
)

@Suppress("FunctionName")
fun <M, K, P> StackProjectorTail(
    scope: CoroutineScope,
    modelsStack: StateFlow<NonEmptyStack<M>>,
    extractKey: (M) -> K,
    createProjector: (CoroutineScope, M) -> P,
): StateFlow<StackProjectorTail<K, P>> {

    fun NonEmptyStack<M>.keys(): Set<K> = all.map(extractKey).toSet()

    fun createTailInfo(
        isNew: Boolean?,
        keys: Set<K>,
        model: M,
    ): TailInfo<K, P> {
        val projectorScope = scope.createChild()
        return TailInfo(
            cancel = { projectorScope.cancel() },
            tail = StackProjectorTail(
                projector = createProjector(projectorScope, model),
                isNew = isNew,
                key = extractKey(model),
            ),
            stackKeys = keys,
        )
    }

    return modelsStack
        .runningFoldState(
            scope = scope,
            createInitial = { stack ->
                createTailInfo(
                    isNew = null,
                    keys = stack.keys(),
                    model = stack.tail,
                )
            },
            operation = { previousInfo, newStack ->
                val model = newStack.tail
                val key = extractKey(model)
                val keys = newStack.keys()
                if (previousInfo.tail.key == key) {
                    return@runningFoldState previousInfo.copy(
                        stackKeys = keys,
                    )
                }
                previousInfo.cancel()
                createTailInfo(
                    isNew = key !in previousInfo.stackKeys,
                    keys = keys,
                    model = model,
                )
            }
        )
        .mapStateLite(TailInfo<K, P>::tail)
}
