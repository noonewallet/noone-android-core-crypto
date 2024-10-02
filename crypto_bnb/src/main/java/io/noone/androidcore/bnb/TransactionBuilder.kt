package io.noone.androidcore.bnb

import com.google.protobuf.ByteString
import io.noone.androidcore.ECKey
import io.noone.androidcore.utils.sha256
import io.noone.androidcore.bnb.protos.Binance
import io.noone.androidcore.bnb.types.MessageType
import io.noone.androidcore.bnb.types.SignData

@Suppress("SpellCheckingInspection")
class TransactionBuilder(
    private val chainId: String,
    private val accountNumber: Long,
    private val sequence: Long
) {

    @Suppress("SameParameterValue")
    private fun getSignData(
        memo: String,
        source: Long,
        data: ByteArray?, // TODO not implemented!!
        from: String,
        to: String,
        amountRaw: Long,
        denom: String
    ): SignData {
        require(data == null) { "data signing not implemented yet" }
        return SignData(
            chainId = chainId,
            accountNumber = accountNumber.toString(),
            sequence = sequence.toString(),
            memo = memo,
            source = source.toString(),
            from = from,
            to = to,
            amount = amountRaw,
            denom = denom
        )
    }

    private fun encodeTransferMessage(msg: SignData): ByteArray {
        return msg.msgs[0].toProto().toByteArray().aminoWrap(
            MessageType.Send.typePrefixBytes,
            false
        )
    }

    private fun sign(msg: SignData, privateKey: ECKey): ByteArray {
        return privateKey.sign(msg.toByteArray().sha256).toCanonicalised().toCompact()
    }

    private fun encodeSignature(signatureBytes: ByteArray, privateKey: ECKey): ByteArray {
        val pubKeyBytes: ByteArray = privateKey.pubKey
        val pubKeyPrefix: ByteArray = MessageType.PubKey.typePrefixBytes
        val pubKeySignBytes = ByteArray(pubKeyBytes.size + pubKeyPrefix.size + 1)
        System.arraycopy(pubKeyPrefix, 0, pubKeySignBytes, 0, pubKeyPrefix.size)
        pubKeySignBytes[pubKeyPrefix.size] = 33.toByte()
        System.arraycopy(pubKeyBytes, 0, pubKeySignBytes, pubKeyPrefix.size + 1, pubKeyBytes.size)
        val stdSignature: Binance.Signature = Binance.Signature.newBuilder()
            .setPubKey(ByteString.copyFrom(pubKeySignBytes))
            .setSignature(ByteString.copyFrom(signatureBytes))
            .setAccountNumber(accountNumber)
            .setSequence(sequence)
            .build()
        return stdSignature.toByteArray().aminoWrap(
            MessageType.StdSignature.typePrefixBytes,
            false
        )
    }

    private fun encodeTx(
        memo: String,
        source: Long,
        data: ByteArray?,
        msg: ByteArray,
        signature: ByteArray
    ): ByteArray {
        var stdTxBuilder: Binance.Transaction.Builder = Binance.Transaction.newBuilder()
            .addMsgs(ByteString.copyFrom(msg))
            .addSignatures(ByteString.copyFrom(signature))
            .setMemo(memo)
            .setSource(source)
        data?.let {
            stdTxBuilder = stdTxBuilder.setData(ByteString.copyFrom(it))
        }
        return stdTxBuilder.build()
            .toByteArray()
            .aminoWrap(MessageType.StdTx.typePrefixBytes, true)
    }

    fun assemble(
        memo: String,
        source: Long,
        data: ByteArray?,
        from: String,
        to: String,
        amountRaw: Long,
        denom: String,
        pk: ECKey
    ): ByteArray {
        val signData = getSignData(memo, source, null, from, to, amountRaw, denom)
        val signature = sign(signData, pk)
        val encodedMsg = encodeTransferMessage(signData)
        val encodedSign = encodeSignature(signature, pk)
        return encodeTx(memo, source, data, encodedMsg, encodedSign)
    }

}