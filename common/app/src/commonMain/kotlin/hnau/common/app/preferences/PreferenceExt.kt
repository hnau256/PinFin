package hnau.common.app.preferences

import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.mapper.Mapper
import kotlinx.coroutines.CoroutineScope

fun <I, O> Preference<I>.map(
    scope: CoroutineScope,
    mapper: Mapper<I, O>,
): Preference<O> = Preference<O>(
    value = value.mapState(scope) { valueOrNone ->
        valueOrNone.map(mapper.direct)
    },
    update = { newValue ->
        val transformedNewValue = mapper.reverse(newValue)
        update(transformedNewValue)
    }
)