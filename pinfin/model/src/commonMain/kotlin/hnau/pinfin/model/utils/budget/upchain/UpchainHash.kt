package hnau.pinfin.model.utils.budget.upchain

import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringAsBase64ByteArray
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import java.security.MessageDigest

@Serializable(UpchainHash.Serializer::class)
class UpchainHash private constructor(
    val hash: ByteArray,
) {

    override fun toString(): String =
        "UpchainHash(${stringMapper.reverse(this)})"

    override fun equals(
        other: Any?,
    ): Boolean = other
        ?.castOrNull<UpchainHash>()
        ?.takeIf { hash.contentEquals(it.hash) } != null

    override fun hashCode(): Int = hash.contentHashCode()

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
            Mapper.stringAsBase64ByteArray + Mapper(::UpchainHash, UpchainHash::hash)

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