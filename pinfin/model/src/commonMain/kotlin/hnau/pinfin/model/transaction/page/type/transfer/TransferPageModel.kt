package hnau.pinfin.model.transaction.page.type.transfer

import hnau.common.app.model.goback.GoBackHandler

sealed interface TransferPageModel {

    val goBackHandler: GoBackHandler
}