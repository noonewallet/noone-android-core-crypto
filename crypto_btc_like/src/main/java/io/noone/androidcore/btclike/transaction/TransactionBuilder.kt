package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.ErrorMessages
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.types.UInt
import io.noone.androidcore.types.VarInt
import io.noone.androidcore.utils.hex
import java.util.LinkedList

class TransactionBuilder(
    private val witnessProducer: WitnessProducer,
    private val sigPreimageProducer: SigPreimageProducer,
    private val scriptSigProducer: ScriptSigProducer,
    private val scriptPubKeyProducer: ScriptPubKeyProducer,
    private val networkParameters: NetworkParameters,
    private val version: UInt = UInt.of(1)
) {
    companion object {
        private val SEGWIT_MARKER = 0x00.toByte()
        private val SEGWIT_FLAG = 0x01.toByte()
        private val LOCK_TIME = UInt.of(0)
    }

    private val inputs = LinkedList<Input>()
    private val outputs = LinkedList<Output>()

    private var changeAddress: String? = null
    private var fee: Long = 0

    private val change: Long
        get() {
            var income = 0L
            for (input in inputs) {
                val satoshi = input.satoshi
                income += satoshi
            }
            var outcome = 0L
            for (output in outputs) {
                val satoshi = output.satoshi
                outcome += satoshi
            }
            val change = income - outcome - fee
            check(change >= 0) {
                "Not enough satoshi. All inputs: " + income +
                        ". All outputs with fee: " + (outcome + fee)
            }

            check(!(change > 0 && changeAddress == null)) { "Transaction contains change ($change satoshi) but no address to send them to" }

            return change
        }

    fun from(
        fromTransactionBigEnd: ByteArray,
        fromToutNumber: Int,
        closingScript: String,
        satoshi: Long,
        privateKey: PrivateKey,
        sequence: Sequence = Sequence.ZERO
    ): TransactionBuilder {
        Input(
            fromTransactionBigEnd,
            fromToutNumber,
            closingScript,
            satoshi,
            privateKey,
            witnessProducer,
            scriptSigProducer,
            sequence.value,
            networkParameters
        ).let { inputs.add(it) }
        return this
    }

    fun from(
        unspentOutput: UnspentOutput,
        sequence: Sequence = Sequence.ZERO
    ): TransactionBuilder {
        Input(
            unspentOutput.txHash.hex,
            unspentOutput.txOutputN,
            unspentOutput.script,
            unspentOutput.value,
            unspentOutput.privateKey,
            witnessProducer,
            scriptSigProducer,
            sequence.value,
            networkParameters
        ).let { inputs.add(it) }
        return this
    }

    fun to(address: String, value: Long): TransactionBuilder {
        Output(
            value,
            address,
            OutputType.CUSTOM,
            scriptPubKeyProducer,
            networkParameters
        ).let { outputs.add(it) }
        return this
    }

    fun changeTo(changeAddress: String): TransactionBuilder {
        this.changeAddress = changeAddress
        return this
    }

    fun withFee(fee: Long): TransactionBuilder {
        require(fee >= 0) { ErrorMessages.FEE_NEGATIVE }
        this.fee = fee
        return this
    }

    fun build(): Transaction {
        check(!inputs.isEmpty()) { "Transaction must contain at least one input" }

        val buildOutputs = LinkedList(outputs)

        if (change > 0) {
            buildOutputs.add(
                Output(
                    change,
                    changeAddress!!,
                    OutputType.CHANGE,
                    scriptPubKeyProducer,
                    networkParameters
                )
            )
        }

        check(!buildOutputs.isEmpty()) { "Transaction must contain at least one output" }

        val buildSegWitTransaction = containsSegwitInput()

        return Transaction(networkParameters).apply {
            addData(Transaction.TxPart.VERSION, version.toString())

            if (buildSegWitTransaction) {
                addData(Transaction.TxPart.MARKER, byteArrayOf(SEGWIT_MARKER).hex)
                addData(Transaction.TxPart.FLAG, byteArrayOf(SEGWIT_FLAG).hex)
            }

            addData(Transaction.TxPart.INPUT_COUNT, VarInt.of(inputs.size.toLong()).toString())
            val witnesses = LinkedList<ByteArray>()
            for (i in inputs.indices) {
                val sigHash = getSigHash(buildOutputs, i)
                inputs[i].fillTransaction(sigHash, this, ScriptType.forLock(inputs[i].lock))
                if (buildSegWitTransaction) {
                    witnesses.add(inputs[i].getWitness(sigHash))
                }
            }

            addData(
                Transaction.TxPart.OUTPUT_COUNT,
                VarInt.of(buildOutputs.size.toLong()).toString()
            )
            for (output in buildOutputs) {
                output.fillTransaction(this)
            }

            if (buildSegWitTransaction) {
                addHeader(Transaction.TxPart.WITNESSES)
                for (w in witnesses) {
                    addData(Transaction.TxPart.WITNESS, w.hex)
                }
            }

            addData(Transaction.TxPart.LOCKTIME, LOCK_TIME.toString())

            setFee(fee)
        }
    }

    private fun getSigHash(
        buildOutputs: List<Output>,
        signedInputIndex: Int
    ): ByteArray {
        return sigPreimageProducer.produceSigHash(
            version,
            LOCK_TIME,
            inputs,
            signedInputIndex,
            buildOutputs
        )
    }

    private fun containsSegwitInput(): Boolean = inputs.any { it.isSegWit }

    override fun toString(): String {
        val result = StringBuilder()

        result.appendLine("Network: ${networkParameters::class.java.name}")
        if (inputs.size > 0) {
            result.appendLine("Segwit transaction: ").append(containsSegwitInput())
            var sum = 0L
            for (input in inputs) {
                val satoshi = input.satoshi
                sum += satoshi
            }
            result.appendLine("Inputs: ").append(sum)
            for (i in inputs.indices) {
                result.appendLine("   ").append(i + 1).append(". ").append(inputs[i])
            }
        }
        if (outputs.size > 0) {
            var sum = 0L
            for (output in outputs) {
                val satoshi = output.satoshi
                sum += satoshi
            }
            result.appendLine("Outputs: ").append(sum)
            for (i in outputs.indices) {
                result.appendLine("   ").append(i + 1).append(". ").append(outputs[i])
            }
        }
        changeAddress?.let {
            result.appendLine("Change to: ").append(it)
        }
        if (fee > 0) {
            result.appendLine("Fee: ").append(fee)
        }
        result.appendLine()

        return result.toString()
    }

}