package hnau.common.app.storage

import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.operationOrNullIfExecuting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

data class InstantStorageEntry<T>(
    val value: T,
    val updateValue: ((T) -> Unit)?,
)


fun <T> StorageEntry<T>.toInstant(
    scope: CoroutineScope,
): StateFlow<InstantStorageEntry<T>> = combineState(
    scope = scope,
    a = value,
    b = operationOrNullIfExecuting(
        scope = scope,
        action = ::updateValue,
    ),
    combine = ::InstantStorageEntry,
)
