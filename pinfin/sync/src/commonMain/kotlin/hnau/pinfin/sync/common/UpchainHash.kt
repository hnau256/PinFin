package hnau.pinfin.sync.common

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringAsHexByteArray
import hnau.common.kotlin.serialization.MappingKSerializer
import hnau.pinfin.upchain.Update
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import java.security.MessageDigest

@Serializable(UpchainHash.Serializer::class)
@JvmInline
value class UpchainHash private constructor(
    val hash: ByteArray,
) {

    operator fun plus(
        update: Update,
    ): UpchainHash {
        val updateBytes = update.value.toByteArray(Charsets.UTF_8)
        val hashWithUpdateBytes = hash + updateBytes
        val newHash = createDigest().digest(hashWithUpdateBytes)
        return UpchainHash(
            hash = newHash,
        )
    }

    override fun toString(): String =
        "UpchainHash($stringMapper.reverse(this))"

    object Serializer : MappingKSerializer<String, UpchainHash>(
        base = String.serializer(),
        mapper = stringMapper,
    )

    companion object {

        val stringMapper: Mapper<String, UpchainHash> =
            Mapper.stringAsHexByteArray + Mapper(::UpchainHash, UpchainHash::hash)

        private fun createDigest(): MessageDigest =
            MessageDigest.getInstance("SHA-256")

        val empty = UpchainHash(
            hash = byteArrayOf(),
        )
    }
}