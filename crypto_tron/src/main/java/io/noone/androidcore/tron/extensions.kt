package io.noone.androidcore.tron

import java.math.BigInteger

fun Long.encodeVarint(): ByteArray {
    val byteArray = mutableListOf<Byte>()

    var v = this
    while (v >= 128) {
        byteArray.add((v and 0x7F or 0x80).toByte())
        v = v ushr 7
    }
    byteArray.add(v.toByte())

    return byteArray.toByteArray()
}

fun BigInteger.encodeVarint(): ByteArray {
    val byteArray = mutableListOf<Byte>()

    var v = this
    while (v >= BigInteger.valueOf(128)) {
        byteArray.add((v and BigInteger.valueOf(0x7F) or BigInteger.valueOf(0x80)).toByte())
        v = v.shiftRight(7)
    }
    byteArray.add(v.toByte())

    return byteArray.toByteArray()
}