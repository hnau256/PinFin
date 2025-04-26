package hnau.pinfin.upchain.impl

import hnau.pinfin.upchain.BudgetUpchain
import hnau.pinfin.upchain.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class FileBasedBudgetUpchain(
    scope: CoroutineScope,
    private val budgetFile: File,
) : BudgetUpchain {

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
                    val updates = lines.map(::Update)
                    block(updates)
                }
            }
        }
    }

    override suspend fun addUpdates(
        updates: List<Update>,
    ) {
        accessUpdatesMutex.withLock {
            withContext(Dispatchers.IO) {
                budgetFile.parentFile.mkdirs()
                FileOutputStream(budgetFile, true).use { output ->
                    updates.forEach { update ->
                        val line: ByteArray = update
                            .value
                            .toByteArray(charset)
                        output.write(line)
                        output.write(linesSeparator)
                    }
                }
            }
        }
    }

    companion object {

        private val charset: Charset = Charsets.UTF_8

        private val linesSeparator: ByteArray = "\n".toByteArray(charset)
    }
}