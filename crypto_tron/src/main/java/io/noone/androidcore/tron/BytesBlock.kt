package io.noone.androidcore.tron

class BytesBlock(
    override val type: ByteArray,
    private val value: ByteArray
) : DynamicSizeBlock<ByteArray>(type, value) {

    override val size: ByteArray
        get() = value.size.toLong().encodeVarint()

    override fun encode(): ByteArray {
        return byteArrayOf(*type, *size, *value)
    }

}