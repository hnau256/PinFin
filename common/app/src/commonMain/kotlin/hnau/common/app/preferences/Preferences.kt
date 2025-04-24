package hnau.common.app.preferences

import kotlinx.coroutines.CoroutineScope

fun interface Preferences {

    operator fun get(
        key: String,
    ): Preference<String>

    interface Factory {

        suspend fun createPreferences(
            scope: CoroutineScope,
        ): Preferences
    }
}