package io.noone.androidcore.tron

import java.math.BigInteger

class TransferContract private constructor(
    override val type: ByteArray = Contract.OBJECT_TYPE
): ObjectBlock(type), Contract {

    companion object {
        val TYPE_ADDRESS_FROM = byteArrayOf(0x0a)
        val TYPE_ADDRESS_TO = byteArrayOf(0x12)
        val TYPE_AMOUNT = byteArrayOf(0x18)
        
        val TRANSFER_TYPE: BigInteger = BigInteger.ONE
    }
    constructor(
        addressFrom: ByteArray,
        addressTo: ByteArray,
        amount: BigInteger
    ): this() {

        val value = ObjectBlock(Contract.TYPE_VALUE).apply {
            add(BytesBlock(TYPE_ADDRESS_FROM, addressFrom))
            add(BytesBlock(TYPE_ADDRESS_TO, addressTo))
            add(VarIntBlock(TYPE_AMOUNT, amount))
        }
        val params = ObjectBlock(Contract.TYPE_PARAMS).apply {
            add(
                BytesBlock(
                Contract.TYPE_CONTRACT_NAME,
                contractType.toByteArray(Charsets.UTF_8)
            )
            )
            add(value)
        }

        add(VarIntBlock(Contract.TYPE_CONTRACT_OBJECT, TRANSFER_TYPE))
        add(params)
    }

    override val contractType: String
        get() = "type.googleapis.com/protocol.TransferContract"
}