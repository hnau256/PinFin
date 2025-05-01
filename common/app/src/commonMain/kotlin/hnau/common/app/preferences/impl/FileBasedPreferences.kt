package hnau.common.app.preferences.impl

import arrow.core.toOption
import hnau.common.app.preferences.Preference
import hnau.common.app.preferences.Preferences
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToStringsBySeparator
import hnau.common.kotlin.mapper.stringToStringsPairBySeparator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

class FileBasedPreferences(
    private val scope: CoroutineScope,
    initialValues: Map<String, String>,
    private val updateValues: suspend (Map<String, String>) -> Unit,
) : Preferences {

    private val values: MutableStateFlow<Map<String, String>> =
        initialValues.toMutableStateFlowAsInitial()

    private val updateMutex = Mutex()

    override fun get(
        key: String,
    ): Preference<String> = Preference(
        value = values.mapState(scope) { values ->
            values[key].toOption()
        },
        update = { newValue ->
            updateMutex.withLock {
                val newValues = values.value + (key to newValue)
                updateValues(newValues)
                values.value = newValues
            }
        }
    )

    class Factory(
        private val preferencesFile: File,
    ) : Preferences.Factory {

        override suspend fun createPreferences(
            scope: CoroutineScope,
        ): Preferences {
            val text = withContext(Dispatchers.IO) {
                preferencesFile
                    .takeIf { it.exists() }
                    ?.readText(charset = charset)
            }
            val initialValues = withContext(Dispatchers.Default) {
                text
                    ?.let(stringToValuesMapper.direct)
                    .orEmpty()
            }
            return FileBasedPreferences(
                scope = scope,
                initialValues = initialValues,
                updateValues = { newValues ->
                    val text = withContext(Dispatchers.Default) {
                        newValues.let(stringToValuesMapper.reverse)
                    }
                    withContext(Dispatchers.IO) {
                        preferencesFile.writeText(
                            text = text,
                            charset = charset,
                        )
                    }
                }
            )
        }

        companion object {

            private val stringToValuesMapper: Mapper<String, Map<String, String>> = run {

                val entryMapper: Mapper<String, Pair<String, String>> =
                    Mapper.stringToStringsPairBySeparator(
                        separator = ':',
                    )

                val entriesMapper = Mapper<List<String>, Map<String, String>>(
                    direct = { it.associate(entryMapper.direct) },
                    reverse = { it.toList().map(entryMapper.reverse) }
                )

                val entriesStringsMapper: Mapper<String, List<String>> =
                    Mapper.stringToStringsBySeparator(separator = '\n')

                entriesStringsMapper + entriesMapper
            }

            private val charset: Charset = Charsets.UTF_8
        }
    }
}