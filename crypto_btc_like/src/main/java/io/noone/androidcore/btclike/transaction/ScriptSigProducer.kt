package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.opcodes.OpCodes
import io.noone.androidcore.btclike.opcodes.OpSize
import io.noone.androidcore.utils.ByteBuffer

open class ScriptSigProducer {

    protected open val sigHashType = SigHashType.ALL.asByte()

    open fun produceScriptSig(
        sigHash: ByteArray,
        key: PrivateKey,
        scriptType: ScriptType
    ): ByteArray {
        return when (scriptType) {
            ScriptType.P2WPKH -> byteArrayOf()
            ScriptType.P2PKH -> {
                ByteBuffer().apply {
                    val publicKey = key.publicKey
                    append(*key.sign(sigHash))
                    append(sigHashType)
                    putFirst(OpSize.ofInt(this.size))
                    append(OpSize.ofInt(publicKey.size))
                    append(*publicKey)
                }
                    .bytes
            }
            ScriptType.P2SH -> {
                ByteBuffer().apply {
                    val pubKeyHash = key.key.pubKeyHash
                    append(OpCodes.FALSE)
                    append(pubKeyHash.size.toByte())
                    append(*pubKeyHash)
                    putFirst(OpSize.ofInt(this.size))
                }
                    .bytes
            }
            else -> throw IllegalArgumentException()
        }
    }

}
