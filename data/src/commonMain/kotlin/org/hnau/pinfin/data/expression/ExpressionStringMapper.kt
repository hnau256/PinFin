package org.hnau.pinfin.data.expression

import org.hnau.commons.kotlin.mapper.Mapper

private val expressionStringMapper: Mapper<String, Expression> = Mapper(
    direct = { string ->
        Expression
            .parseOrNull(string)
            ?: error("Unable parse arithmetic expression from '$string'")
    },
    reverse = Expression::serialize,
)

val Expression.Companion.stringMapper: Mapper<String, Expression>
    get() = expressionStringMapper