package io.noone.androidcore.tron

abstract class DynamicSizeBlock<T: Any>(
    override val type: ByteArray,
    private val value: T
) : Block {

    abstract val size: ByteArray
}