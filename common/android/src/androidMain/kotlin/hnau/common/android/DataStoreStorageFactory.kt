package hnau.common.android

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import hnau.common.app.storage.Storage
import hnau.common.app.storage.textBased
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream

fun Storage.Factory.Companion.dataStore(
    scope: CoroutineScope,
    context: Context,
): Storage.Factory {
    val dataStoreFactory = DataStoreFactory.create(
        serializer = object : Serializer<String?> {

            override val defaultValue get() = null

            override suspend fun readFrom(
                input: InputStream,
            ): String? = input
                .readBytes()
                .takeIf { it.isNotEmpty() }
                ?.toString(charset)

            override suspend fun writeTo(
                t: String?,
                output: OutputStream,
            ) = output.write(
                t
                    ?.toByteArray(charset)
                    ?: byteArrayOf(),
            )
        },
        produceFile = { context.dataStoreFile("storage") },
        scope = scope,
    )
    return Storage.Factory.textBased(
        read = { dataStoreFactory.data.first() },
        write = { newData -> dataStoreFactory.updateData { newData } },
    )
}

private val charset = Charsets.UTF_8
