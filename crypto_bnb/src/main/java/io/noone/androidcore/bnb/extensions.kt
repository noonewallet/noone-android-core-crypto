package io.noone.androidcore.bnb

import io.noone.androidcore.ECKey
import io.noone.androidcore.bnb.types.VarLong
import java.math.BigInteger

fun ECKey.ECDSASignature.toCompact(): ByteArray {
    val result = ByteArray(64)
    System.arraycopy(r.bigIntToBytes(32), 0, result, 0, 32)
    System.arraycopy(s.bigIntToBytes( 32), 0, result, 32, 32)
    return result
}

fun BigInteger.bigIntToBytes(numBytes: Int): ByteArray {
    require(this.signum() >= 0) { "number must be positive or zero" }
    require(numBytes > 0) { "num" }
    val src = this.toByteArray()
    val dest = ByteArray(numBytes)
    val isFirstByteOnlyForSign = src[0] == 0.toByte()
    val length = if (isFirstByteOnlyForSign) src.size - 1 else src.size
    require(length <= numBytes) { "The given number does not fit in $numBytes" }
    val srcPos = if (isFirstByteOnlyForSign) 1 else 0
    val destPos = numBytes - length
    System.arraycopy(src, srcPos, dest, destPos, length)
    return dest
}

fun ByteArray.aminoWrap(typePrefix: ByteArray, isPrefixLength: Boolean): ByteArray {
    var totalLen = size + typePrefix.size
    val varLong = VarLong(totalLen.toLong())
    if (isPrefixLength) totalLen += varLong.size()
    return ByteSerializer(totalLen).let { serializer ->
        if (isPrefixLength) {
            serializer.write(VarLong((size + typePrefix.size).toLong()).toBytes())
        }
        serializer.write(typePrefix)
        serializer.write(this)
        serializer.serialize()
    }
}