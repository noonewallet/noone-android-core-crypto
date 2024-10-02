package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.utils.ByteBuffer
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.opcodes.OpSize

open class WitnessProducer {

    open fun produceWitness(
        sigHash: ByteArray,
        key: PrivateKey
    ): ByteArray {
        val sign = ByteBuffer(*key.sign(sigHash), SigHashType.ALL.asByte())
        val pubKeyEncoded = key.key.pubKeyPoint.getEncoded(true)
        return ByteBuffer().apply {
            append(0x02.toByte())
            append(OpSize.ofInt(sign.size))
            append(*sign.bytes())
            append(OpSize.ofInt(pubKeyEncoded.size))
            append(*pubKeyEncoded)
        }
            .bytes
    }
}