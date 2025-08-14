package hnau.pinfin.model.transaction.part

import hnau.pinfin.model.transaction.page.PageModel
import kotlinx.coroutines.CoroutineScope

sealed interface PartModel {

    fun createPage(
        scope: CoroutineScope,
            ): PageModel
}