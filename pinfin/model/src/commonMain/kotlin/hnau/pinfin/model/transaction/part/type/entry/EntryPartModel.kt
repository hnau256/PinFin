package hnau.pinfin.model.transaction.part.type.entry

import hnau.pinfin.model.transaction.page.type.entry.EntryPagePageModel
import kotlinx.coroutines.CoroutineScope

interface EntryPartModel {

    fun createPage(
        scope: CoroutineScope,
            ): EntryPagePageModel
}