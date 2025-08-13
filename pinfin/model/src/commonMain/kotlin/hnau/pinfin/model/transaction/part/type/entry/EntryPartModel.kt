package hnau.pinfin.model.transaction.part.type.entry

import hnau.pinfin.model.transaction.page.type.entry.EntryPageModel
import hnau.pinfin.model.transaction.utils.NavAction
import kotlinx.coroutines.CoroutineScope

interface EntryPartModel {

    fun createPage(
        scope: CoroutineScope,
        navAction: NavAction,
    ): EntryPageModel
}