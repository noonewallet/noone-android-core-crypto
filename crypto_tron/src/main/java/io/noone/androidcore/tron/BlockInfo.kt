package io.noone.androidcore.tron

import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.toBytesBE
import java.math.BigInteger

class BlockInfo(
    val blockId: String,
    val timestamp: Long,
    val blockHeight: BigInteger,
) {

    val blockBytes: ByteArray
        get() {
            return blockHeight.toLong().toBytesBE().copyOfRange(6, 8)
        }

    val refBlockHash: ByteArray
        get() {
            return blockId.hex.copyOfRange(8, 16)
        }

    fun getExpiration(offset: Long = 60000L): Long {
        return timestamp + offset
    }
}