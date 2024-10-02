package io.noone.androidcore.bnb

import io.noone.androidcore.utils.*
import java.math.BigInteger

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ByteSerializer @JvmOverloads constructor(initialSize: Int = 0) {

    private var bytes: ByteArray
    private var lastIndex = 0

    init {
        bytes = ByteArray(initialSize)
    }

    private fun ensureCapacity(additionalBytes: Int) {
        if (bytes.size < lastIndex + additionalBytes) {
            val temp = ByteArray(lastIndex + additionalBytes)
            System.arraycopy(bytes, 0, temp, 0, bytes.size)
            bytes = temp
        }
    }

    @JvmOverloads
    fun write(bytes: ByteArray, offset: Int = 0, count: Int = bytes.size): ByteSerializer {
        ensureCapacity(count)
        System.arraycopy(bytes, offset, this.bytes, lastIndex, count)
        this.lastIndex += count
        return this
    }

    fun writeLE(bytes: ByteArray): ByteSerializer = write(bytes.toBytesLE())

    fun write(value: Byte): ByteSerializer = write(byteArrayOf(value))

    fun write(value: Boolean): ByteSerializer = write((if (value) 1 else 0).toByte())

    fun writeLE(value: Short): ByteSerializer = write(value.toBytesLE())

    fun write(value: Int): ByteSerializer = write(value.toBytes())

    fun writeLE(value: Int): ByteSerializer = write(value.toBytesLE())

    fun writeBE(value: Int): ByteSerializer = write(value.toBytesBE())

    fun writeLE(value: Long): ByteSerializer = write(value.toBytesLE())

    fun write(value: String): ByteSerializer = write(value.toByteArray())

    fun writeLE(value: String): ByteSerializer = writeLE(value.toByteArray())

    fun write(bigInteger: BigInteger, numBytes: Int): ByteSerializer {
        return write(bigInteger.bigIntToBytes(numBytes))
    }

    fun writeHex(hex: String): ByteSerializer = write(HexUtils.toBytes(hex))

    fun serialize(): ByteArray = bytes

    override fun toString(): String = serialize().hex
}