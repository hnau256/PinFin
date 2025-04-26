package hnau.common.kotlin.mapper

import java.util.Base64

private val stringAsBase64ByteArrayMapper = Mapper<String, ByteArray>(
    direct = { str -> Base64.getDecoder().decode(str) },
    reverse = { bytes -> Base64.getEncoder().encodeToString(bytes) }
)

val Mapper.Companion.stringAsBase64ByteArray: Mapper<String, ByteArray>
    get() = stringAsBase64ByteArrayMapper

private val stringAsHexByteArrayMapper = Mapper<String, ByteArray>(
    direct = { hex ->
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        hex
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    },
    reverse = { bytes ->
        bytes.joinToString("") { "%02x".format(it) }
    }
)

val Mapper.Companion.stringAsHexByteArray: Mapper<String, ByteArray>
    get() = stringAsHexByteArrayMapper