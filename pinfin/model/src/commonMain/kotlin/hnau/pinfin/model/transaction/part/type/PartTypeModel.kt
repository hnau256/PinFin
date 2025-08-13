package hnau.pinfin.model.transaction.part.type

import hnau.pinfin.model.transaction.page.type.PageTypeModel
import hnau.pinfin.model.transaction.utils.NavAction
import kotlinx.coroutines.CoroutineScope

sealed interface PartTypeModel {

    fun createPage(
        scope: CoroutineScope,
        navAction: NavAction,
    ): PageTypeModel

    sealed interface Skeleton
}