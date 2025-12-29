package io.noone.adnroidcore.cardano.crypto

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for CardanoEd25519 implementation.
 * Test vectors generated from Native C++ implementation.
 */
class CardanoEd25519Test {

    // CIP-3 Icarus test vector (without passphrase)
    // Mnemonic: "eight country switch draw meat scout mystery blade tip drift useless good keep usage title"
    companion object {
        // Root key (96 bytes = 64 private + 32 chain code)
        private const val ROOT_KEY = "c065afd2832cd8b087c4d9ab7011f481ee1e0721e78ea5dd609f3ab3f156d245" +
                "d176bd8fd4ec60b4731c3918a2a72a0226c0cd119ec35b47e4d55884667f552a"
        private const val ROOT_CHAIN_CODE = "23f7fdcd4a10c6cd2c7393ac61d877873e248f417634aa3d812af327ffe9d620"

        // Test vectors from Native implementation
        private const val ROOT_PUBLIC_KEY = "757e95578798ef733ad93be322fb043053d56b445d3fe502bcf7cb4a6b0f0c6a"
        private const val ROOT_SIGNATURE = "2d935eab722a3563d2afc5c3ff8f56fe9c681a9125cb52cc2d9d98c02318577cee1059b5c80ad9b6220dbb19a3a84ab9896fcc6ef9878c59eaed7cbc5c06a600"

        // Derivation path m/1852'
        private const val KEY_1852H = "98f25c3313e03b7843072514c5f024782072406b37569403423d2361f356d2459ecc73a09adb2aa37e9f8530fd1e6745eee1ea248a85417a700e774182c7fa3d"
        private const val CC_1852H = "864cf884b7faf31f33733e6fc900d4446394d8d6ee55610473ef24c6bb3fb91f"

        // Derivation path m/1852'/1815'
        private const val KEY_1852H_1815H = "504e9dc3ed6d22231a0e43cd250fcda7757b42aa9affae5f20dea1fef956d2450cc3d2abd5b6a6626d73d789a6ad77cd0e5d92e093dc5a1484c71e4f996f495e"
        private const val CC_1852H_1815H = "b84879a6adef2f9d283e8a1815cfb946ee5ce70873632097bb723ac2565d8002"

        // Derivation path m/1852'/1815'/0' (account)
        private const val ACCOUNT_KEY = "f80081fa05eece83236e612463aafad20d6b92eee67479a1977959540057d2452173fe9a0fccf61cf2cc7c52638f2ded6c08002a71424ca5b93681ee7a385828"
        private const val ACCOUNT_CC = "332b13689518700be3c6d330d72490c42e8a98b7495889a27851e543319fb095"
        private const val ACCOUNT_PUBLIC_KEY = "7f376415131590bf8cc88e8466fd24a6f95eebd6c2271d89cb51a81402618c9b"

        // Derivation path m/1852'/1815'/0'/0 (external chain)
        private const val EXTERNAL_KEY = "00b5105515ee89a7718c0bdbe05c9613a17a5907f5943dacfde9802f0157d2458bddfc042979fb8b30cb13437edc7ec7c1836676e063780d5db0b8ae84babe4e"
        private const val EXTERNAL_CC = "47b1b5663a3bddf78d9698ca580263af68a907eed0dd6421ef2daf62c20d61b9"

        // Derivation path m/1852'/1815'/0'/0/0 (first address)
        private const val ADDR0_KEY = "00df3ecf0e02979dd9ee569d09412c1f370f476054aaa1ef3cf5a08c0557d245a6ad0fe81ab55e36178f5866dc8f83cf57239fdeee35c737ef887964aae20500"
        private const val ADDR0_PUBLIC_KEY = "cc9809944150c00f3913cd2b103e9b42fe6243fc36a76f9eb800692e2bda3f2e"

        // Derivation path m/1852'/1815'/0'/2/0 (stake key)
        private const val STAKE_KEY = "f874f575cb9b7dabfd5487e9151c759fffc66d6915f86f8b08a4e21f0957d245cd81a43dc6716e6d7fd574b31e7e91c1b3a6968e4aa69e880712f196500a2713"
        private const val STAKE_PUBLIC_KEY = "6162765320c93ad3c82cc28b9578be31a791f03a37dcae056343cc25bbcb3b31"

        // HMAC-SHA512("test-key", "test-message")
        private const val HMAC_TEST = "f24ca47730e976b85cf6ee888e17aa94c671fe4b4b4de8900462d56bb15d85ecdcb851ea406d6600644f7ceae32e2cb66973e81174dea47a33c5a257f0a6b7d7"
    }

    private val rootKey = ROOT_KEY.hexToByteArray()
    private val rootCC = ROOT_CHAIN_CODE.hexToByteArray()

    @Test
    fun testPublicKeyDerivation() {
        val publicKey = CardanoEd25519.publicKey(rootKey)
        assertEquals(ROOT_PUBLIC_KEY, publicKey.toHex())
    }

    @Test
    fun testSignature() {
        val message = "Hello, Cardano!".toByteArray()
        val signature = CardanoEd25519.sign(message, rootKey)
        assertEquals(ROOT_SIGNATURE, signature.toHex())
    }

    @Test
    fun testHmacSha512() {
        val key = "test-key".toByteArray()
        val message = "test-message".toByteArray()
        val hmac = CardanoEd25519.hmacSha512(key, message)
        assertEquals(HMAC_TEST, hmac.toHex())
    }

    @Test
    fun testDerivation1852H() {
        val derived = CardanoEd25519.deriveChildKey(rootKey, rootCC, 1852, true)
        val key = derived.copyOfRange(0, 64)
        val cc = derived.copyOfRange(64, 96)

        assertEquals(KEY_1852H, key.toHex())
        assertEquals(CC_1852H, cc.toHex())
    }

    @Test
    fun testDerivation1852H_1815H() {
        // First derive 1852'
        val d1 = CardanoEd25519.deriveChildKey(rootKey, rootCC, 1852, true)
        val key1 = d1.copyOfRange(0, 64)
        val cc1 = d1.copyOfRange(64, 96)

        // Then derive 1815'
        val d2 = CardanoEd25519.deriveChildKey(key1, cc1, 1815, true)
        val key2 = d2.copyOfRange(0, 64)
        val cc2 = d2.copyOfRange(64, 96)

        assertEquals(KEY_1852H_1815H, key2.toHex())
        assertEquals(CC_1852H_1815H, cc2.toHex())
    }

    @Test
    fun testAccountKeyDerivation() {
        // Derive m/1852'/1815'/0'
        var key = rootKey
        var cc = rootCC

        for (index in listOf(1852, 1815, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, true)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        assertEquals(ACCOUNT_KEY, key.toHex())
        assertEquals(ACCOUNT_CC, cc.toHex())

        val publicKey = CardanoEd25519.publicKey(key)
        assertEquals(ACCOUNT_PUBLIC_KEY, publicKey.toHex())
    }

    @Test
    fun testExternalChainDerivation() {
        // Derive m/1852'/1815'/0'
        var key = rootKey
        var cc = rootCC

        for (index in listOf(1852, 1815, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, true)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        // Derive /0 (soft, external chain)
        val derived = CardanoEd25519.deriveChildKey(key, cc, 0, false)
        val extKey = derived.copyOfRange(0, 64)
        val extCC = derived.copyOfRange(64, 96)

        assertEquals(EXTERNAL_KEY, extKey.toHex())
        assertEquals(EXTERNAL_CC, extCC.toHex())
    }

    @Test
    fun testFirstAddressDerivation() {
        // Derive m/1852'/1815'/0'/0/0
        var key = rootKey
        var cc = rootCC

        // Hardened part
        for (index in listOf(1852, 1815, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, true)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        // Soft derivation /0/0
        for (index in listOf(0, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, false)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        assertEquals(ADDR0_KEY, key.toHex())

        val publicKey = CardanoEd25519.publicKey(key)
        assertEquals(ADDR0_PUBLIC_KEY, publicKey.toHex())
    }

    @Test
    fun testStakeKeyDerivation() {
        // Derive m/1852'/1815'/0'/2/0
        var key = rootKey
        var cc = rootCC

        // Hardened part
        for (index in listOf(1852, 1815, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, true)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        // Soft derivation /2/0 (stake chain)
        for (index in listOf(2, 0)) {
            val derived = CardanoEd25519.deriveChildKey(key, cc, index, false)
            key = derived.copyOfRange(0, 64)
            cc = derived.copyOfRange(64, 96)
        }

        assertEquals(STAKE_KEY, key.toHex())

        val publicKey = CardanoEd25519.publicKey(key)
        assertEquals(STAKE_PUBLIC_KEY, publicKey.toHex())
    }

    // Helper extensions
    private fun String.hexToByteArray(): ByteArray {
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
