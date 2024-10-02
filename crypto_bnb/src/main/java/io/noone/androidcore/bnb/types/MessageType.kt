package io.noone.androidcore.bnb.types

import io.noone.androidcore.utils.HexUtils

enum class MessageType(typePrefix: String?) {
    Send("2A2C87FA"),
    StdSignature(null),
    PubKey("EB5AE987"),
    StdTx("F0625DEE");

    val typePrefixBytes: ByteArray =
        typePrefix?.let { HexUtils.toBytes(typePrefix) } ?: ByteArray(0)

}
