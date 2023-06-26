package io.noone.androidcore.types

import io.noone.androidcore.utils.hex

class VarInt private constructor(value: Long) {

    companion object {
        fun of(value: Long): VarInt = VarInt(value)
    }

    private val litEndBytes: ByteArray

    init {
        litEndBytes = toBytes(value)
    }

    override fun toString(): String = litEndBytes.hex

    fun asLitEndBytes(): ByteArray = litEndBytes

    private fun toBytes(value: Long): ByteArray {
        return when (value) {
            in 0..0xfc -> {
                byteArrayOf(value.toByte())
            }
            in 1..0xFFFF -> {
                byteArrayOf(
                    0xFD.toByte(),
                    value.toByte(),
                    (value shr 8).toByte()
                )
            }
            in 1..0xFFFFFFFFL -> {
                byteArrayOf(
                    0xFE.toByte(),
                    value.toByte(),
                    (value shr 8).toByte(),
                    (value shr 16).toByte(),
                    (value shr 24).toByte()
                )
            }
            else -> {
                byteArrayOf(
                    0xFF.toByte(),
                    value.toByte(),
                    (value shr 8).toByte(),
                    (value shr 16).toByte(),
                    (value shr 24).toByte(),
                    (value shr 32).toByte(),
                    (value shr 40).toByte(),
                    (value shr 48).toByte(),
                    (value shr 56).toByte()
                )
            }
        }
    }

}