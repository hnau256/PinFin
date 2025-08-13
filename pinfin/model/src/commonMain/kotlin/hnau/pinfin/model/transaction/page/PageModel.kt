package hnau.pinfin.model.transaction.page

import hnau.common.app.model.goback.GoBackHandler

sealed interface PageModel {

    val goBackHandler: GoBackHandler
}