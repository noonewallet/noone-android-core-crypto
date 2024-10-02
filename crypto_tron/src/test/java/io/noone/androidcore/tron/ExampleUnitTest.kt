package io.noone.androidcore.tron

import io.noone.androidcore.ECKey
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.hd.MnemonicCode
import io.noone.androidcore.utils.hex
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import java.math.BigInteger

class ExampleUnitTest {

    companion object {
        const val DERIVATION_PATH = "44'/195'/0'/0/0"
    }

    @Test
    fun addressGeneration() {
        val privateKey = "F43EBCC94E6C257EDBE559183D1A8778B2D5A08040902C0F0A77A3343A1D0EA5".hex

        val expectedPublicKey =
            "04e6a49d098ee94871252622b8a8b727e5cdf81b7138e5ac16591887f6e8c10e881e4b4250fa8c87f5b29ad020216a9ffd0acf5995a627d6c70e4dd274c54c20bd".hex

        val ecKey = ECKey.fromPrivate(privateKey)
        val pubKey = ecKey.pubKeyPoint.getEncoded(false)

        Assert.assertArrayEquals(pubKey, expectedPublicKey)

        val exceptedEncodedAddress = "TWVRXXN5tsggjUCDmqbJ4KxPdJKQiynaG6"
        val expectedHash = "e11973395042ba3c0b52b4cdf4e15ea77818f275".hex
        val expectedPrefix = 0x41

        Assert.assertEquals(expectedPrefix, TronAddress.PREFIX)

        val tronAddress = TronAddress(pubKey)
        Assert.assertEquals(exceptedEncodedAddress, tronAddress.address)
        Assert.assertArrayEquals(expectedHash, tronAddress.hash)

        val tronAddressFromECKey = TronAddress.fromEcKey(ecKey)
        Assert.assertEquals(exceptedEncodedAddress, tronAddressFromECKey.address)
        Assert.assertArrayEquals(expectedHash, tronAddressFromECKey.hash)

        val seed = MnemonicCode.toSeed(
            listOf(
                "symptom",
                "sister",
                "young",
                "regular",
                "seat",
                "divorce",
                "foot",
                "gadget",
                "hospital",
                "action",
                "derive",
                "rude"
            ), ""
        )
        val key = DeterministicSeed(seed).getKeyFromSeed(DERIVATION_PATH)
        val myAddress = TronAddress.fromEcKey(key)
        Assert.assertEquals("TXokWASgJVDSBzPWaDLqfCsAWu3bmnWSE1", myAddress.toString())

    }

    @Test
    fun testVarIntSerialization() {
        val expectedValue = "40e8dcd5b9a731".hex
        val input = 1694217105000L.toBigInteger()
        val block = VarIntBlock(byteArrayOf(0x40.toByte()), input)

        val encoded = block.encode()
        Assert.assertArrayEquals(expectedValue, encoded)
    }

    @Test
    fun testBytesSerialization() {
        val expectedValue = "2208ead284d495f255ad".hex
        val input = "ead284d495f255ad".hex
        val block = BytesBlock(byteArrayOf(0x22.toByte()), input)

        val encoded = block.encode()
        Assert.assertArrayEquals(expectedValue, encoded)


        val expectedLongValue = "12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        val longInput = "0a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        val longBlock = BytesBlock(byteArrayOf(0x12.toByte()), longInput)

        val longEncoded = longBlock.encode()
        Assert.assertArrayEquals(expectedLongValue, longEncoded)
    }

    @Test
    fun testTextToBytes() {
        val expectedBytes = "747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e7472616374".hex
        val string = "type.googleapis.com/protocol.TriggerSmartContract"

        Assert.assertArrayEquals(string.toByteArray(), expectedBytes)
        Assert.assertEquals(expectedBytes.toString(Charsets.UTF_8), string)
    }

    @Test
    fun testObjectEncoding() {
        val expectedBytes = "5aae01081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex

        val dataBlockInput = "0a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        val dataBlock = BytesBlock(byteArrayOf(0x12.toByte()), dataBlockInput)
        val typeBlock = VarIntBlock(byteArrayOf(0x08.toByte()), 31L.toBigInteger())

        val objectBlock = ObjectBlock(byteArrayOf(0x5a.toByte())).apply {
            this.add(typeBlock)
            this.add(dataBlock)
        }

        val encoded = objectBlock.encode()

        Assert.assertArrayEquals(expectedBytes, encoded)
    }

    @Test
    fun testVarint() {
        val value = 100000000L
        val expectedVarint = "80c2d72f".hex
        Assert.assertArrayEquals(expectedVarint, value.encodeVarint())
    }

    @Test
    fun testTransactionEncoding() {
        val expectedRaw = "0a021f902208ead284d495f255ad40e8dcd5b9a7315aae01081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd000000000000000000000000000000000000000000000000000000000000006470c397d2b9a731900180c2d72f".hex

        val triggerSmartContract = TriggerSmartContract(
            addressFrom = "41977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb".hex,
            contract = "419e62be7f4f103c36507cb2a753418791b1cdc182".hex,
            input = "095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        )

        val tx = TransactionBuilder(allowFeeLimit = true)
            .setRefBlockBytes("1f90".hex)
            .setRefBlockHash("ead284d495f255ad".hex)
            .setExpirationTime(1694217105000L)
            .setContract(triggerSmartContract)
            .setTimestamp(1694217046979L)
            .setEnergyLimit(100000000L.toBigInteger())
            .build()

        println(tx.raw)
        Assert.assertArrayEquals(expectedRaw, tx.raw)
    }

    @Test
    fun testContractEncoding() {
        val expectedTransfer = "5a65080112610a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412300a15418840e6c55b9ada326d211d818c34a994aeced808121541d3136787e667d1e055d2cd5db4b5f6c8805630491864".hex

        val transferContract = TransferContract(
            addressFrom = "418840e6c55b9ada326d211d818c34a994aeced808".hex,
            addressTo = "41d3136787e667d1e055d2cd5db4b5f6c880563049".hex,
            amount = BigInteger.valueOf(100L)
        )

        Assert.assertArrayEquals(expectedTransfer, transferContract.encode())

        val expectedContractTrigger = "5aae01081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex

        val triggerSmartContract = TriggerSmartContract(
            addressFrom = "41977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb".hex,
            contract = "419e62be7f4f103c36507cb2a753418791b1cdc182".hex,
            input = "095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        )

        Assert.assertArrayEquals(expectedContractTrigger, triggerSmartContract.encode())
    }

    @Test
    fun testSignatureEncoding() {
        val expectedSignedTransaction = "0A8A010A0202DB2208C89D4811359A28004098A4E0A6B52D5A730802126F0A32747970652E676F6F676C65617069732E636F6D2F70726F746F636F6C2E5472616E736665724173736574436F6E747261637412390A07313030303030311215415A523B449890854C8FC460AB602DF9F31FE4293F1A15416B0580DA195542DDABE288FEC436C7D5AF769D24206412418BF3F2E492ED443607910EA9EF0A7EF79728DAAAAC0EE2BA6CB87DA38366DF9AC4ADE54B2912C1DEB0EE6666B86A07A6C7DF68F1F9DA171EEE6A370B3CA9CBBB00".hex
        val expectedSignature = "12418BF3F2E492ED443607910EA9EF0A7EF79728DAAAAC0EE2BA6CB87DA38366DF9AC4ADE54B2912C1DEB0EE6666B86A07A6C7DF68F1F9DA171EEE6A370B3CA9CBBB00".hex

        val transaction = BytesBlock(byteArrayOf(0x0A), "0A0202DB2208C89D4811359A28004098A4E0A6B52D5A730802126F0A32747970652E676F6F676C65617069732E636F6D2F70726F746F636F6C2E5472616E736665724173736574436F6E747261637412390A07313030303030311215415A523B449890854C8FC460AB602DF9F31FE4293F1A15416B0580DA195542DDABE288FEC436C7D5AF769D242064".hex)
        val signature = BytesBlock(byteArrayOf(0x12), "8BF3F2E492ED443607910EA9EF0A7EF79728DAAAAC0EE2BA6CB87DA38366DF9AC4ADE54B2912C1DEB0EE6666B86A07A6C7DF68F1F9DA171EEE6A370B3CA9CBBB00".hex)

        Assert.assertArrayEquals(expectedSignature, signature.encode())

        val signedTransaction = ObjectBlock().apply {
            add(transaction)
            add(signature)
        }.encodeBlocksOnly()

        Assert.assertArrayEquals(expectedSignedTransaction, signedTransaction)

        val privateKey = "F43EBCC94E6C257EDBE559183D1A8778B2D5A08040902C0F0A77A3343A1D0EA5".hex

        val triggerSmartContract = TriggerSmartContract(
            addressFrom = "41977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb".hex,
            contract = "419e62be7f4f103c36507cb2a753418791b1cdc182".hex,
            input = "095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex
        )

        val tx = TransactionBuilder(allowFeeLimit = true)
            .setRefBlockBytes("1f90".hex)
            .setRefBlockHash("ead284d495f255ad".hex)
            .setExpirationTime(1694217105000L)
            .setContract(triggerSmartContract)
            .setTimestamp(1694217046979L)
            .setEnergyLimit(100000000L.toBigInteger())
            .build()

        val sign = ECKey.fromPrivate(privateKey).signWithV(tx.txID)

        val txId = tx.txID

        Assert.assertArrayEquals("0b9631904926413e732c04fe0e258d06336a7b470604291ceee861328f865806".hex, txId)

        val signedTx = SignedTransaction(tx, sign.toByteArray())

        Assert.assertArrayEquals(
            "0ad3010a021f902208ead284d495f255ad40e8dcd5b9a7315aae01081f12a9010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412740a1541977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb1215419e62be7f4f103c36507cb2a753418791b1cdc1822244095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd000000000000000000000000000000000000000000000000000000000000006470c397d2b9a731900180c2d72f124196fca1e69936c5e6d17156b56ef882b6943c2c230643843fd2c86c3d31cde0107e66e689a21322eb70c6a8767727cde47a44f68a198c9b8f4534485f902bd64e00".hex,
            signedTx.raw
        )

        val transferContract = TransferContract(
            addressFrom = "418840e6c55b9ada326d211d818c34a994aeced808".hex,
            addressTo = "41d3136787e667d1e055d2cd5db4b5f6c880563049".hex,
            amount = BigInteger.valueOf(1100000L)
        )

        val tx1 = TransactionBuilder(allowFeeLimit = false)
            .setRefBlockBytes("1f90".hex)
            .setRefBlockHash("ead284d495f255ad".hex)
            .setExpirationTime(1694217105000L)
            .setContract(transferContract)
            .setTimestamp(1694217046979L)
            .build()

        val signedTx1 = SignedTransaction(tx1, sign.toByteArray())
        println(tx1.raw.size + 67 + 3 + 64)
        println(264 + BigInteger.valueOf(1100000L).encodeVarint().size)
    }
}