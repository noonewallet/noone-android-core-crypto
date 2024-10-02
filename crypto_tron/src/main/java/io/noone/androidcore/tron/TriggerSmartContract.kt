package io.noone.androidcore.tron

import java.math.BigInteger

class TriggerSmartContract private constructor(
    override val type: ByteArray = Contract.OBJECT_TYPE
): ObjectBlock(type), Contract {

    companion object {
        val TYPE_ADDRESS_FROM = byteArrayOf(0x0a)
        val TYPE_CONTRACT = byteArrayOf(0x12)
        val TYPE_INPUT = byteArrayOf(0x22)

        val TRIGGER_SMART_CONTRACT_TYPE: BigInteger = BigInteger.valueOf(0x1f)
    }

    constructor(
        addressFrom: ByteArray,
        contract: ByteArray,
        input: ByteArray
    ): this() {
        val value = ObjectBlock(Contract.TYPE_VALUE).apply {
            add(BytesBlock(TYPE_ADDRESS_FROM, addressFrom))
            add(BytesBlock(TYPE_CONTRACT, contract))
            add(BytesBlock(TYPE_INPUT, input))
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

        add(VarIntBlock(Contract.TYPE_CONTRACT_OBJECT, TRIGGER_SMART_CONTRACT_TYPE))
        add(params)
    }

    override val contractType: String
        get() = "type.googleapis.com/protocol.TriggerSmartContract"
}