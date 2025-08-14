package hnau.pinfin.model.transaction.page.type

import hnau.common.app.model.goback.GoBackHandler

sealed interface TypePageModel {

    val goBackHandler: GoBackHandler
}