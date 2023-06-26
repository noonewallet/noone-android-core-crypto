package io.noone.androidcore.types

import io.noone.androidcore.utils.hex

class UInt private constructor(value: Int) {
    var litEndBytes: ByteArray

    init {
        litEndBytes = byteArrayOf(
            value.toByte(),
            (value shr 8).toByte(),
            (value shr 16).toByte(),
            (value shr 24).toByte()
        )
    }

    fun asLitEndBytes(): ByteArray {
        return litEndBytes
    }

    fun asByte(): Byte {
        if (litEndBytes[1].toInt() != 0 ||
            litEndBytes[2].toInt() != 0 ||
            litEndBytes[3].toInt() != 0
        ) {
            throw IllegalStateException("The satoshi is more than 255 and can't be represented as one byte")
        }
        return litEndBytes[0]
    }

    override fun toString(): String = litEndBytes.hex

    companion object {
        fun of(value: Int): UInt = UInt(value)

        fun of(value: Byte): UInt = UInt(value.toInt())
    }
}