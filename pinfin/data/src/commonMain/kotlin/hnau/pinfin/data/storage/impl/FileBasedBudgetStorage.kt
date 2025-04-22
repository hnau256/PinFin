package hnau.pinfin.data.storage.impl

import hnau.pinfin.data.dto.BudgetId
import hnau.pinfin.data.dto.Update
import hnau.pinfin.data.storage.BudgetStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class FileBasedBudgetStorage(
    scope: CoroutineScope,
    override val id: BudgetId,
    private val budgetFile: File,
) : BudgetStorage {

    private val accessUpdatesMutex: Mutex = Mutex()

    override suspend fun <R> useUpdates(
        block: (Sequence<Update>) -> R,
    ): R = accessUpdatesMutex.withLock {
        withContext(Dispatchers.IO) {
            when (budgetFile.exists()) {
                false -> block(emptySequence())
                true -> budgetFile.useLines(
                    charset = charset,
                ) { lines ->
                    val updates = lines.map { line ->
                        json.decodeFromString(
                            deserializer = Update.serializer(),
                            string = line,
                        )
                    }
                    block(updates)
                }
            }
        }
    }

    override suspend fun addUpdate(
        update: Update,
    ) {
        accessUpdatesMutex.withLock {
            val line: ByteArray = json
                .encodeToString(
                    serializer = Update.serializer(),
                    value = update
                )
                .toByteArray(charset)
            withContext(Dispatchers.IO) {
                budgetFile.parentFile.mkdirs()
                FileOutputStream(budgetFile, true).use { output ->
                    output.write(line)
                    output.write(linesSeparator)
                }
            }
        }
    }

    companion object {

        private val charset: Charset = Charsets.UTF_8

        private val linesSeparator: ByteArray = "\n".toByteArray(charset)

        private val json: Json = Json.Default
    }
}