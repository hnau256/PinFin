package hnau.pinfin.model.transaction.part.type.transfer

import hnau.pinfin.model.transaction.page.type.entry.EntryPagePageModel
import kotlinx.coroutines.CoroutineScope

interface TransferPartModel {

    fun createPage(
        scope: CoroutineScope,
            ): EntryPagePageModel
}