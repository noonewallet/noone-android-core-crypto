package io.noone.androidcore.crypto

import io.noone.androidcore.ECKey
import io.noone.androidcore.hd.ChildNumber
import io.noone.androidcore.hd.DeterministicKey
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.hd.MnemonicCode
import io.noone.androidcore.utils.HexUtils
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.keccak256
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger

class Test {

    companion object {
        private const val BTC_PATH = "44'/0'/0'"

    }

    private fun getDeterministicKey(): DeterministicKey {
        return DeterministicKey(
            listOf(
                ChildNumber(44, true),
                ChildNumber(0, true),
                ChildNumber(0, true),
                ChildNumber(1, false),
            ),
            "4f2fdfca96ec761c6baaa7c9b8ccd36ec2e9b3612d1c752fe25e924baf3541a5".hex,
            BigInteger("3180aaa8770d3fb796565e6ef657a58374ee066c0da28ee4c2e2d9efa29fb7f1", 16),
            null
        )
    }

    private fun getECKey(): ECKey {
        val privKey = "3180aaa8770d3fb796565e6ef657a58374ee066c0da28ee4c2e2d9efa29fb7f1".hex
        return ECKey.fromPrivate(privKey)
    }

    @Test
    fun testMnemonicToSeed() {
        val expectedSeed = "abed1ce3f13984cf92ad0120119b94892c0e6ee2c4c00769d69c3808b97c513cb24783ecbb0055c4832ed07cddde1a75c61fd5769b63f63f70b70db9e9f89747"
        val mnemonicString = listOf("symptom", "sister", "young", "regular", "seat", "divorce", "foot", "gadget", "hospital", "action", "derive", "rude")
        val seed = MnemonicCode.toSeed(mnemonicString, "")
        Assert.assertEquals(expectedSeed, seed.hex)
    }

    @Test
    fun test_DeterministicSeed_getKeyFromSeed() {
        val expectedAccountPrivateKey = "3180aaa8770d3fb796565e6ef657a58374ee066c0da28ee4c2e2d9efa29fb7f1"
        val seed = "abed1ce3f13984cf92ad0120119b94892c0e6ee2c4c00769d69c3808b97c513cb24783ecbb0055c4832ed07cddde1a75c61fd5769b63f63f70b70db9e9f89747".hex

        val btcAccount = DeterministicSeed(seed).getKeyFromSeed(BTC_PATH)

        Assert.assertEquals(expectedAccountPrivateKey, btcAccount.privKeyBytes.hex)
    }

    @Test
    fun test_DeterministicSeed_companion_createMasterPrivateKey() {
        val expectedMasterKey = "e4a05b99028103799739ca8395fecc21409b5005a350b491f1bbef819d9efc71"

        val seed = "abed1ce3f13984cf92ad0120119b94892c0e6ee2c4c00769d69c3808b97c513cb24783ecbb0055c4832ed07cddde1a75c61fd5769b63f63f70b70db9e9f89747".hex
        val masterKey = DeterministicSeed.createMasterPrivateKey(seed)

        Assert.assertEquals(expectedMasterKey, masterKey.privKeyBytes.hex)
    }

    @Test
    fun test_DeterministicSeed_createMasterPrivateKey() {
        val expectedMasterKey = "e4a05b99028103799739ca8395fecc21409b5005a350b491f1bbef819d9efc71"

        val seedBytes = "abed1ce3f13984cf92ad0120119b94892c0e6ee2c4c00769d69c3808b97c513cb24783ecbb0055c4832ed07cddde1a75c61fd5769b63f63f70b70db9e9f89747".hex
        val seed = DeterministicSeed(seedBytes)

        Assert.assertEquals(expectedMasterKey, seed.createMasterPrivateKey().privKeyBytes.hex)
    }

    @Test
    fun test_DeterministicKey_deriveChildKey() {
        val expectedPrivateKey = "005a3bace60275e1177aed6f547acb6c226662b27bc75ad3f08a0e92969e4760"

        val key = getDeterministicKey()
        val derivedKey = key.deriveChildKey(ChildNumber.ZERO)

        Assert.assertEquals(expectedPrivateKey, derivedKey.privKeyBytes.hex)
    }

    @Test
    fun test_DeterministicKey_identifier() {
        val expectedIdentifier = "a94391e178bcc2e905cead0ded0deda6b13e1b61"
        val key = getDeterministicKey()

        Assert.assertEquals(expectedIdentifier, key.identifier.hex)
    }

    @Test
    fun test_DeterministicKey_pathAsString() {
        val expectedPath = "M/44H/0H/0H/1"
        val key = getDeterministicKey()

        Assert.assertEquals(expectedPath, key.pathAsString)
    }

    @Test
    fun test_ECKEY_pubKeyHash() {
        val expectedHash = "a94391e178bcc2e905cead0ded0deda6b13e1b61"
        val ecKey = getECKey()

        Assert.assertEquals(expectedHash, ecKey.pubKeyHash.hex)
    }

    @Test
    fun test_ECKey_privateKeyAsHex() {
        val expectedPrivateKey = "3180aaa8770d3fb796565e6ef657a58374ee066c0da28ee4c2e2d9efa29fb7f1"
        val ecKey = getECKey()
        Assert.assertEquals(expectedPrivateKey, ecKey.privateKeyAsHex)
    }

    @Test
    fun test_ECKey_publicKeyAsHex() {
        val expectedPublicKey = "0382df5fed2938ebe1bcc6b3760b4605242d5f61682a4143bb5049d1b67b6e33c0"
        val ecKey = getECKey()

        Assert.assertEquals(expectedPublicKey, ecKey.publicKeyAsHex)
    }

    @Test
    fun test_hexKeccak256() {
        val expectedKeccakString = "70a08231b98ef4ca268c9cc3f6b4590e4bfec28280db06bb5d45e689f2a360be"
        val keccak256 = "balanceOf(address)".keccak256.hex

        Assert.assertEquals(expectedKeccakString, keccak256)
    }

    @Test
    fun test_HexUtils_toHex() {
        val expectedString = "70a08231b98ef4ca268c9cc3f6b4590e4bfec28280db06bb5d45e689f2a360be"
        val expectedSubString = "8231b98e"
        val expectedStringWithSeparator = "70-a0-82-31-b9-8e-f4-ca-26-8c-9c-c3-f6-b4-59-0e-4b-fe-c2-82-80-db-06-bb-5d-45-e6-89-f2-a3-60-be"
        val expectedSubStringWithSeparator = "82-31-b9-8e"
        val bytes = byteArrayOf(112, -96, -126, 49, -71, -114, -12, -54, 38, -116, -100, -61, -10, -76, 89, 14, 75, -2, -62, -126, -128, -37, 6, -69, 93, 69, -26, -119, -14, -93, 96, -66)

        Assert.assertEquals(expectedString, HexUtils.toHex(bytes))
        Assert.assertEquals(expectedStringWithSeparator, HexUtils.toHex(bytes, "-"))
        Assert.assertEquals(expectedSubString, HexUtils.toHex(bytes, 2, 4, null))
        Assert.assertEquals(expectedSubStringWithSeparator, HexUtils.toHex(bytes, 2, 4, "-"))

    }

    @Test
    fun test_HexUtils_toBytes() {
        val string = "70a08231b98ef4ca268c9cc3f6b4590e4bfec28280db06bb5d45e689f2a360be"
        val stringWrongLength = "000"
        val stringWrongChars = "12weqq34DaKa"
        val expectedBytes = byteArrayOf(112, -96, -126, 49, -71, -114, -12, -54, 38, -116, -100, -61, -10, -76, 89, 14, 75, -2, -62, -126, -128, -37, 6, -69, 93, 69, -26, -119, -14, -93, 96, -66)

        Assert.assertArrayEquals(expectedBytes, HexUtils.toBytes(string))
        Assert.assertThrows(IllegalArgumentException::class.java) {
            HexUtils.toBytes(stringWrongLength)
        }
        Assert.assertThrows(IllegalArgumentException::class.java) {
            HexUtils.toBytes(stringWrongChars)
        }
    }

}