package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.ErrorMessages
import io.noone.androidcore.btclike.opcodes.OpCodes
import io.noone.androidcore.btclike.opcodes.OpSize
import io.noone.androidcore.utils.ByteBuffer

class ScriptPubKeyProducer {

    fun produceScript(
        hash: ByteArray,
        type: ScriptType
    ): ByteArray {
        return when (type) {
            ScriptType.P2SH -> {
                ByteBuffer(
                    OpCodes.HASH160,
                    OpSize.ofInt(hash.size),
                    *hash,
                    OpCodes.EQUAL
                )
            }
            ScriptType.P2PKH -> {
                ByteBuffer(
                    OpCodes.DUP,
                    OpCodes.HASH160,
                    OpSize.ofInt(hash.size),
                    *hash,
                    OpCodes.EQUALVERIFY,
                    OpCodes.CHECKSIG
                )
            }
            ScriptType.P2WPKH -> {
                ByteBuffer(
                    OpCodes.FALSE,
                    OpSize.ofInt(hash.size),
                    *hash
                )
            }
            else -> throw IllegalArgumentException(
                String.format(ErrorMessages.SPK_UNSUPPORTED_PRODUCER, type.toString())
            )
        }.bytes
    }

}


