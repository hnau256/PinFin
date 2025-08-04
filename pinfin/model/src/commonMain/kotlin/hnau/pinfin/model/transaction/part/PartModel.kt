package hnau.pinfin.model.transaction.part

import hnau.pinfin.model.transaction.part.page.PageModel
import hnau.pinfin.model.transaction.utils.NavAction
import kotlinx.coroutines.CoroutineScope

sealed interface PartModel {

    fun createPage(
        scope: CoroutineScope,
        navAction: NavAction,
    ): PageModel

    sealed interface Skeleton
}