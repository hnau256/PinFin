package hnau.pinfin.model.transaction_old_2.page.type.entry

import hnau.common.app.model.goback.GoBackHandler

sealed interface EntryPagePageModel {

    val goBackHandler: GoBackHandler
}