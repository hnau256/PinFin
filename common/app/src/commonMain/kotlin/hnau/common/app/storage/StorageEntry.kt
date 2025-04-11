package hnau.common.app.storage

import kotlinx.coroutines.flow.StateFlow

interface StorageEntry<T> {

    val value: StateFlow<T>

    suspend fun updateValue(newValue: T)
}
