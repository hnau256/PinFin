package hnau.pinfin.model.utils.budget.storage.impl

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.utils.getUpdatesAfterHashIfPossible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class FileBasedUpchainStorage(
    initialUpchain: Upchain,
    private val budgetFile: File,
) : UpchainStorage {

    private val accessUpdatesMutex: Mutex = Mutex()

    private val _upchain: MutableStateFlow<Upchain> =
        initialUpchain.toMutableStateFlowAsInitial()

    override val upchain: StateFlow<Upchain>
        get() = _upchain

    override suspend fun setNewUpchain(
        newUpchain: Upchain,
    ) {
        accessUpdatesMutex.withLock {
            val currentUpchain = _upchain.value
            val updates = newUpchain.getUpdatesAfterHashIfPossible(
                hash = currentUpchain.peekHash,
            )
            when (updates) {
                //TODO use temporary file
               null ->  writeUpdates(
                   updates = newUpchain.items.map(Upchain.Item::update),
                   replace = true,
               )
                else -> writeUpdates(
                    updates = updates,
                    replace = false,
                )
            }
            _upchain.value = newUpchain
        }
    }

    private suspend fun writeUpdates(
        updates: List<Update>,
        replace: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            budgetFile.parentFile.mkdirs()
            val append = !replace
            FileOutputStream(budgetFile, append).use { output ->
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

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            budgetFile: File,
        ): FileBasedUpchainStorage {
            val updatesFromFile: List<Update> = withContext(Dispatchers.IO) {
                budgetFile
                    .takeIf(File::exists)
                    ?.useLines(
                        charset = charset,
                    ) { lines ->
                        lines
                            .map(::Update)
                            .toList()
                    }
                    ?: emptyList()
            }
            val initialUpchain: Upchain = withContext(Dispatchers.Default) {
                updatesFromFile
                    .fold(
                        initial = Upchain.empty,
                        operation = Upchain::plus,
                    )
            }
            return FileBasedUpchainStorage(
                initialUpchain = initialUpchain,
                budgetFile = budgetFile,
            )
        }

        private val charset: Charset = Charsets.UTF_8

        private val linesSeparator: ByteArray = "\n".toByteArray(charset)
    }
}