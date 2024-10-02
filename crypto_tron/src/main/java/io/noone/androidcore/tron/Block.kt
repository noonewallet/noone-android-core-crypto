package io.noone.androidcore.tron

interface Block {

    val type: ByteArray

    fun encode(): ByteArray
}