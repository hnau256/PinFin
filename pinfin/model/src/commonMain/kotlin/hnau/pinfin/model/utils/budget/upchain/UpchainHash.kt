package hnau.pinfin.model.utils.budget.upchain

import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringAsBase64ByteArray
import hnau.common.kotlin.serialization.MappingKSerializer
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

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
            sha256: Sha256,
        ): UpchainHash {
            val updateBytes = update.value.toByteArray(Charsets.UTF_8)
            val hashWithUpdateBytes = when (previous) {
                null -> updateBytes
                else -> previous.hash + updateBytes
            }
            val newHash = sha256.calcSha256(hashWithUpdateBytes)
            return UpchainHash(
                hash = newHash,
            )
        }

        val stringMapper: Mapper<String, UpchainHash> =
            Mapper.stringAsBase64ByteArray + Mapper(::UpchainHash, UpchainHash::hash)
    }
}


fun UpchainHash?.calcNext(
    update: Update,
    sha256: Sha256,
): UpchainHash = UpchainHash.create(
    previous = this,
    update = update,
    sha256 = sha256,
)