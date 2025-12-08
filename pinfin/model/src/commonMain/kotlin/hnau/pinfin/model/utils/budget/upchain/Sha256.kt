package hnau.pinfin.model.utils.budget.upchain

interface Sha256 {

    fun calcSha256(
        data: ByteArray,
    ): ByteArray
}