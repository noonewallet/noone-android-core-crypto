package io.noone.androidcore.btclike.transaction.bch

import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.transaction.ScriptSigProducer
import io.noone.androidcore.btclike.transaction.ScriptType
import io.noone.androidcore.btclike.transaction.SigHashType

class BchScriptSigProducer : ScriptSigProducer() {

    override val sigHashType: Byte
        get() = SigHashType.BCH_ALL.asByte()

    override fun produceScriptSig(
        sigHash: ByteArray,
        key: PrivateKey,
        scriptType: ScriptType
    ): ByteArray {
        if (scriptType != ScriptType.P2PKH)
            throw IllegalArgumentException()

        return super.produceScriptSig(sigHash, key, scriptType)
    }

}