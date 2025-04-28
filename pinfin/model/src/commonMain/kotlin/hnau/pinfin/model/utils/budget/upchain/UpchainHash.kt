package hnau.pinfin.model.utils.budget.upchain

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringAsHexByteArray
import hnau.common.kotlin.serialization.MappingKSerializer
import hnau.pinfin.model.utils.budget.upchain.Update
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import java.security.MessageDigest

@Serializable(UpchainHash.Serializer::class)
@JvmInline
value class UpchainHash private constructor(
    val hash: ByteArray,
) {

    override fun toString(): String =
        "UpchainHash($stringMapper.reverse(this))"

    object Serializer : MappingKSerializer<String, UpchainHash>(
        base = String.serializer(),
        mapper = stringMapper,
    )

    companion object {

        fun create(
            previous: UpchainHash?,
            update: Update,
        ): UpchainHash {
            val updateBytes = update.value.toByteArray(Charsets.UTF_8)
            val hashWithUpdateBytes = when (previous) {
                null -> updateBytes
                else -> previous.hash + updateBytes
            }
            val newHash = createDigest().digest(hashWithUpdateBytes)
            return UpchainHash(
                hash = newHash,
            )
        }

        val stringMapper: Mapper<String, UpchainHash> =
            Mapper.stringAsHexByteArray + Mapper(::UpchainHash, UpchainHash::hash)

        private fun createDigest(): MessageDigest =
            MessageDigest.getInstance("SHA-256")
    }
}


operator fun UpchainHash?.plus(
    update: Update,
): UpchainHash = UpchainHash.create(
    previous = this,
    update = update,
)