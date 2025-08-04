package hnau.pinfin.model.transaction.part.page

import hnau.common.app.model.goback.GoBackHandler

sealed interface PageModel {

    sealed interface Skeleton

    val goBackHandler: GoBackHandler
}