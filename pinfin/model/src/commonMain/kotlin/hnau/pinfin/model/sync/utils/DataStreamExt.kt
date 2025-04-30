package hnau.pinfin.model.sync.utils

import java.io.DataInputStream
import java.io.DataOutputStream

fun DataInputStream.readSizeWithBytes(): ByteArray {
    val size = readInt()
    return readNBytes(size)
}

fun DataOutputStream.writeSizeWithBytes(
    bytes: ByteArray,
) {
    writeInt(bytes.size)
    write(bytes)
    flush()
}