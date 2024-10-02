package io.noone.androidcore.tron

import io.noone.androidcore.utils.ByteBuffer

open class ObjectBlock(
    override val type: ByteArray = byteArrayOf()
) : Block {

    protected val blocks = mutableListOf<Block>()

    fun add(block: Block) {
        blocks.add(block)
    }

    override fun encode(): ByteArray {
        return ByteBuffer().apply {
            blocks.forEach {
                append(*it.encode())
            }
            putFirst(*this.size.toLong().encodeVarint())
            putFirst(*type)
        }.bytes
    }

    fun encodeBlocksOnly(): ByteArray {
        return ByteBuffer().apply {
            blocks.forEach { append(*it.encode()) }
        }.bytes
    }
}