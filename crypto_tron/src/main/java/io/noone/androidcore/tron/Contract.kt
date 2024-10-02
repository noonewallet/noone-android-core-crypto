package io.noone.androidcore.tron

interface Contract: Block {

    companion object {
        val OBJECT_TYPE = byteArrayOf(0x5a)

        val TYPE_VALUE = byteArrayOf(0x12)
        val TYPE_PARAMS = byteArrayOf(0x12)
        val TYPE_CONTRACT_NAME = byteArrayOf(0x0a)
        val TYPE_CONTRACT_OBJECT = byteArrayOf(0x08)
    }

    val contractType: String
}