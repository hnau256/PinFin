package hnau.common.app.storage

import hnau.common.kotlin.coroutines.mapStateLite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Storage(
    initialValues: Map<String, String>,
    private val updateValues: suspend (Map<String, String>) -> Unit,
) {

    fun interface Factory {

        suspend fun createStorage(): Storage

        companion object
    }

    private val values: MutableStateFlow<Map<String, String>> = MutableStateFlow(initialValues)

    private val updateValuesMutex = Mutex()

    fun string(
        key: String,
        defaultValue: String,
    ): StorageEntry<String> = object : StorageEntry<String> {

        override val value: StateFlow<String> = values.mapStateLite { values ->
            values.getOrElse(key) { defaultValue }
        }

        override suspend fun updateValue(
            newValue: String,
        ) = updateValuesMutex.withLock {
            val newValues = values.updateAndGet { oldValues ->
                oldValues + (key to newValue)
            }
            updateValues(newValues)
        }
    }

    companion object
}
