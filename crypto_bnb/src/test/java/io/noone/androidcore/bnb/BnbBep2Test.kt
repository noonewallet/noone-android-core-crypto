package io.noone.androidcore.bnb

import io.noone.adnrodicore.crypto.hd.ChildNumber
import io.noone.adnrodicore.crypto.hd.DeterministicSeed
import io.noone.adnrodicore.crypto.utils.hex
import org.junit.Assert
import org.junit.Test

class BnbBep2Test {

    private val seed = "c4eb38a2e9a353c180e8e74c474e9192633f81afdbaa49d0c6207def597dafc1".hex

    @Test
    fun test_Address() {
        val expectedAddress = "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua"

        val key = DeterministicSeed.createMasterPrivateKey(seed).deriveChildKey(ChildNumber.ZERO)
        val address = Address(key)

        Assert.assertEquals(expectedAddress, address.toString())
    }

    @Test
    fun test_TransactionBuilder() {
        val expectedEncodedTx = "c201f0625dee0a482a2c87fa0a200a149260e1a82c0f394549eb2f4a79f9d15ce586d4ab12080a03424e4210b90a12200a149260e1a82c0f394549eb2f4a79f9d15ce586d4ab12080a03424e4210b90a126e0a26eb5ae98721034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b11240b149d59ac7bd7a607eda2aed050caf370bd0a1488e8efcba4e5451255f81c058081f9f3a35f5743bda876a60fa5749c9dffe470c24c37e0ea16a947e83d1ced1180020041a002001"
        val expectedEncodedTxWithDataAndMemo = "d101f0625dee0a482a2c87fa0a200a149260e1a82c0f394549eb2f4a79f9d15ce586d4ab12080a03424e4210b90a12200a149260e1a82c0f394549eb2f4a79f9d15ce586d4ab12080a03424e4210b90a126e0a26eb5ae98721034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b112401844b5e5ea123199998d500b54ea78e3543755c1f9b94ab2f66b799a35e5c6c96d16572cc7bc24aee851c2d6d0d34575fc59264d55daf8b434647cad4445723c180020041a046d656d6f20012a09746573742064617461"
        val key = DeterministicSeed.createMasterPrivateKey(seed).deriveChildKey(ChildNumber.ZERO)

        val builder = TransactionBuilder(
            Bep2NetworkParams.CHAIN_ID,
            0L,
            4L
        )
        val encodedTx: ByteArray = builder.assemble(
            "",
            1L,
            null,
            "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
            "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
            1337L,
            "BNB",
            key,
        )

        Assert.assertEquals(expectedEncodedTx, encodedTx.hex)


        val builder1 = TransactionBuilder(
            Bep2NetworkParams.CHAIN_ID,
            0L,
            4L
        )
        val encodedTxWithDataAndMemo: ByteArray = builder1.assemble(
            "memo",
            1L,
            "test data".toByteArray(),
            "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
            "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
            1337L,
            "BNB",
            key,
        )

        Assert.assertEquals(expectedEncodedTxWithDataAndMemo, encodedTxWithDataAndMemo.hex)
    }
}