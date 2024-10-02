package io.noone.androidcore.btclike.transaction.bch

import io.noone.androidcore.btclike.opcodes.OpCodes
import io.noone.androidcore.btclike.opcodes.OpSize
import io.noone.androidcore.btclike.transaction.Input
import io.noone.androidcore.btclike.transaction.Output
import io.noone.androidcore.btclike.transaction.SigHashType
import io.noone.androidcore.btclike.transaction.SigPreimageProducer
import io.noone.androidcore.types.UInt
import io.noone.androidcore.types.ULong
import io.noone.androidcore.utils.ByteBuffer
import io.noone.androidcore.utils.sha256sha256

class BchSigPreimageProducer : SigPreimageProducer() {

    override val sigHashType: UInt = SigHashType.BCH_ALL

    override fun produceSigHash(
        version: UInt,
        lockTime: UInt,
        inputs: List<Input>,
        signedInputIndex: Int,
        buildOutputs: List<Output>
    ): ByteArray {
        return ByteBuffer().apply {
            append(*version.asLitEndBytes())
            append(
                *producePreimage(
                    inputs[signedInputIndex].isSegWit,
                    inputs,
                    buildOutputs,
                    signedInputIndex
                )
            )
            append(*lockTime.asLitEndBytes())
            append(*sigHashType.asLitEndBytes())
        }
            .bytes.sha256sha256
    }

    override fun producePreimage(
        segwit: Boolean,
        inputs: List<Input>,
        outputs: List<Output>,
        singingInputIndex: Int
    ): ByteArray {
        require(!segwit)

        val preimage = ByteBuffer()
        val currentInput = inputs[singingInputIndex]
        //2. hashPrevOuts
        val prevOuts = ByteBuffer()
        inputs.forEach { input ->
            prevOuts.append(*input.transactionHash)
            prevOuts.append(*UInt.of(input.index).litEndBytes)
        }
        preimage.append(*prevOuts.bytes.sha256sha256)

        //3. hashSequences
        val sequences = ByteBuffer()
        for (input in inputs) {
            sequences.append(*input.sequence.asLitEndBytes())
        }
        preimage.append(*sequences.bytes.sha256sha256)

        //4. PreviousTransactionHash
        preimage.append(*currentInput.transactionHash)

        //5.InputIndex
        preimage.append(*UInt.of(currentInput.index).asLitEndBytes())

        val scriptCode = ByteBuffer(
            OpCodes.DUP,
            OpCodes.HASH160,
            OpSize.ofInt(20),
            *currentInput.privateKey.publicKeyHash,
            OpCodes.EQUALVERIFY,
            OpCodes.CHECKSIG
        )

        //6.ScriptDataCount
        preimage.append(OpSize.ofInt(scriptCode.size))

        //7.ScriptData
        preimage.append(*scriptCode.bytes)

        //8.Amount
        preimage.append(*ULong.of(currentInput.satoshi).asLitEndBytes())

        //9.Sequence
        preimage.append(*currentInput.sequence.asLitEndBytes())

        //10.OutputsHash
        val outs = ByteBuffer()
        outputs.forEach { output ->
            outs.append(*output.serializeForSigHash())
        }

        preimage.append(*outs.bytes.sha256sha256)

        return preimage.bytes()
    }
}