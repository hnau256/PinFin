package hnau.pinfin.model.transaction.utils

import arrow.core.NonEmptyList
import arrow.core.tail
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptySetOrNull
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal object IsChangedUtils {

    fun calcIsChanged(
        scope: CoroutineScope,
        children: StateFlow<List<StateFlow<Boolean>>>,
    ): StateFlow<Boolean> = children
        .scopedInState(scope)
        .flatMapState(scope) { (scope, children) ->
            calcIsChanged(
                scope = scope,
                children = children,
            )
        }

    fun calcIsChanged(
        scope: CoroutineScope,
        firstChild: StateFlow<Boolean>,
        vararg childrenTail: StateFlow<Boolean>,
    ): StateFlow<Boolean> = calcIsChanged(
        scope = scope,
        children = NonEmptyList(firstChild, childrenTail.toList()),
    )

     private fun calcIsChanged(
        scope: CoroutineScope,
        children: List<StateFlow<Boolean>>,
    ): StateFlow<Boolean> = children
        .toNonEmptyListOrNull()
        .foldNullable(
            ifNull = { false.toMutableStateFlowAsInitial() },
            ifNotNull = { nonEmptyChildren ->
                nonEmptyChildren
                    .head
                    .scopedInState(scope)
                    .flatMapState(scope) { (scope, head) ->
                        head.foldBoolean(
                            ifTrue = { true.toMutableStateFlowAsInitial() },
                            ifFalse = {
                                calcIsChanged(
                                    scope = scope,
                                    children = nonEmptyChildren.tail,
                                )
                            }
                        )
                    }
            }
        )
}