package hnau.pinfin.model.transaction.page.type.entry

import hnau.common.app.model.goback.GoBackHandler

sealed interface EntryPagePageModel {

    val goBackHandler: GoBackHandler
}