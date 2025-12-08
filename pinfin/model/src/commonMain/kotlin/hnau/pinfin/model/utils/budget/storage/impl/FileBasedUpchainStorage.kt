package hnau.pinfin.model.utils.budget.storage.impl

import hnau.common.app.model.file.File
import hnau.common.app.model.file.exists
import hnau.common.app.model.file.mkDirs
import hnau.common.app.model.file.parent
import hnau.common.app.model.file.sink
import hnau.common.app.model.file.source
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.upchain.Sha256
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.plus
import hnau.pinfin.model.utils.budget.upchain.utils.getUpdatesAfterHashIfPossible
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.readLine

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
        currentUpchainToCheck: Upchain,
        newUpchain: Upchain,
    ): Boolean = accessUpdatesMutex.withLock {
        val currentUpchain = _upchain.value
        if (currentUpchainToCheck != currentUpchain) {
            return@withLock false
        }
        val updates = newUpchain.getUpdatesAfterHashIfPossible(
            hash = currentUpchain.peekHash,
        )
        when (updates) {
            //TODO use temporary file
            null -> writeUpdates(
                updates = newUpchain.items.map(Upchain.Item::update),
                replace = true,
            )

            else -> writeUpdates(
                updates = updates,
                replace = false,
            )
        }
        _upchain.value = newUpchain
        true
    }

    private suspend fun writeUpdates(
        updates: List<Update>,
        replace: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            budgetFile.parent!!.mkDirs()
            budgetFile
                .sink(append = !replace)
                .buffered()
                .use { sink ->
                    updates.forEach { update ->
                        val line: ByteArray = update
                            .value
                            .toByteArray(charset)
                        sink.write(line)
                        sink.write(linesSeparator)
                    }
                }
        }
    }

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            budgetFile: File,
            sha256: Sha256,
        ): FileBasedUpchainStorage {
            val updatesFromFile: List<Update> = withContext(Dispatchers.IO) {
                budgetFile
                    .takeIf(File::exists)
                    ?.source()
                    ?.buffered()
                    ?.use { source ->
                        generateSequence(source::readLine)
                            .map(::Update)
                            .toList()
                    }
                    ?: emptyList()
            }
            val initialUpchain: Upchain = withContext(Dispatchers.Default) {
                Upchain.empty(sha256) + updatesFromFile
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