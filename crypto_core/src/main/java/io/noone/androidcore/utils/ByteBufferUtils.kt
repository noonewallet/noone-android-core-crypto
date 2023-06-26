package io.noone.androidcore.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ByteBufferUtils {

    @JvmStatic
    fun bigEndian(size: Int): ByteBuffer {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)
    }

    @JvmStatic
    fun littleEndian(size: Int): ByteBuffer {
        return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
    }

}
