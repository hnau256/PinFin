package hnau.pinfin.model.transaction_old_2.page.type.transfer

import hnau.common.app.model.goback.GoBackHandler

sealed interface TransferPagePageModel {

    val goBackHandler: GoBackHandler
}