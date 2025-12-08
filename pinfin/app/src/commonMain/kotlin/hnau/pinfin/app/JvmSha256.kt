package hnau.pinfin.app

import hnau.pinfin.model.utils.budget.upchain.Sha256
import java.security.MessageDigest

object JvmSha256 : Sha256 {

    override fun calcSha256(
        data: ByteArray,
    ): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(data)
}