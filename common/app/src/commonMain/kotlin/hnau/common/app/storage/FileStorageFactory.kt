package hnau.common.app.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

@Suppress("FunctionName")
fun FileStorageFactory(
    file: File,
): Storage.Factory = Storage.Factory.textBased(
    read = {
        try {
            file.readText(charset = charset)
        } catch (ex: IOException) {
            null
        }
    },
    write = { newValues ->
        withContext(Dispatchers.IO) {
            file.writeText(
                text = newValues,
                charset = charset,
            )
        }
    },
)

fun Storage.Factory.Companion.file(
    file: File,
): Storage.Factory = FileStorageFactory(
    file = file,
)

private val charset: Charset = Charsets.UTF_8
