package hnau.pinfin.model.transaction.part.type

import hnau.pinfin.model.transaction.page.type.TypePageModel
import kotlinx.coroutines.CoroutineScope

sealed interface TypePartModel {

    fun createPage(
        scope: CoroutineScope,
            ): TypePageModel

    sealed interface Skeleton
}