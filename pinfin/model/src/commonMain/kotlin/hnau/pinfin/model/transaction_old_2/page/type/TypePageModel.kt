package hnau.pinfin.model.transaction_old_2.page.type

import hnau.common.app.model.goback.GoBackHandler

sealed interface TypePageModel {

    val goBackHandler: GoBackHandler
}