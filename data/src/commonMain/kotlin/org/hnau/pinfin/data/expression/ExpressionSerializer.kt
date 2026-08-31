package org.hnau.pinfin.data.expression

import org.hnau.commons.kotlin.foldBoolean

fun Expression.serialize(): String = serializeNode(parent = null, isRightChild = false)

private fun Expression.serializeNode(
    parent: Expression.BinaryOperation.Type?,
    isRightChild: Boolean,
): String = fold(
    ifValue = { value -> value.toPlainString() },

    ifUnaryOperation = { argument, _ -> "-${
        argument
            .serializeNode(parent = null, isRightChild = false)
            .let {
                (argument is Expression.BinaryOperation).foldBoolean(
                    ifTrue = { "($it)" },
                    ifFalse = { it },
                )
            }
    }" },

    ifBinaryOperation = { argument1, argument2, type ->
        val left = argument1.serializeChild(parent = type, isRightChild = false)
        val right = argument2.serializeChild(parent = type, isRightChild = true)
        val expr = "$left${type.symbol}$right"
        needsParens(
            ownType = type,
            parent = parent,
            isRightChild = isRightChild,
        ).foldBoolean(
            ifTrue = { "($expr)" },
            ifFalse = { expr },
        )
    },
)

private fun Expression.serializeChild(
    parent: Expression.BinaryOperation.Type,
    isRightChild: Boolean,
): String {
    val serialized = serializeNode(parent = parent, isRightChild = isRightChild)
    return (this is Expression.UnaryOperation).foldBoolean(
        ifTrue = { "($serialized)" },
        ifFalse = { serialized },
    )
}

private fun needsParens(
    ownType: Expression.BinaryOperation.Type,
    parent: Expression.BinaryOperation.Type?,
    isRightChild: Boolean,
): Boolean {
    if (parent == null) return false
    val ownPriority = ownType.priority
    val parentPriority = parent.priority
    return when {
        ownPriority < parentPriority -> true
        ownPriority == parentPriority && isRightChild -> true
        else -> false
    }
}

private val Expression.BinaryOperation.Type.priority: Int
    get() = fold(
        ifPlus = { 0 },
        ifMinus = { 0 },
        ifTimes = { 1 },
        ifDivide = { 1 },
    )

private val Expression.BinaryOperation.Type.symbol: String
    get() = fold(
        ifPlus = { "+" },
        ifMinus = { "-" },
        ifTimes = { "*" },
        ifDivide = { "/" },
    )
