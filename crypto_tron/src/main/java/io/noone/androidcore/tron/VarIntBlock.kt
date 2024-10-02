package io.noone.androidcore.tron

import java.math.BigInteger


class VarIntBlock(
    override val type: ByteArray,
    private val value: BigInteger
) : Block {

    override fun encode(): ByteArray {
        return byteArrayOf(*type, *(value.encodeVarint()))
    }

}