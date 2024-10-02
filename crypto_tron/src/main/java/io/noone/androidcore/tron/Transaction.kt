package io.noone.androidcore.tron

import io.noone.androidcore.utils.sha256

class Transaction(
    override val type: ByteArray = byteArrayOf(0x0A)
): ObjectBlock(type) {

    val raw: ByteArray
        get() = encodeBlocksOnly()

    val txID: ByteArray
        get() = raw.sha256

}