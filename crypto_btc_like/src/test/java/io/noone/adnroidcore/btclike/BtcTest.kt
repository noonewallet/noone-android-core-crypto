package io.noone.adnroidcore.btclike

import io.noone.androidcore.hd.ChildNumber
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.utils.hex
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.addresses.LegacyAddress
import io.noone.androidcore.btclike.addresses.SegwitAddress
import io.noone.androidcore.btclike.networks.BitcoinParams
import io.noone.androidcore.btclike.transaction.*
import org.junit.Assert
import org.junit.Test

class BtcTest {

    private val seed = "c4eb38a2e9a353c180e8e74c474e9192633f81afdbaa49d0c6207def597dafc1".hex

    @Test
    fun btcAddressGeneration() {
        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val addresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(BitcoinParams, key.deriveChildKey(index)).toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedAddresses, addresses)

        val hardenedAddresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(BitcoinParams, key.deriveChildKey(index, true))
                    .toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedAddresses, hardenedAddresses)

        val bech32Addresses = (0..100)
            .map { index ->
                SegwitAddress.fromKey(BitcoinParams, key.deriveChildKey(index)).toBech32()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedBech32Addresses, bech32Addresses)

        val hardenedBech32Addresses = (0..100)
            .map { index ->
                SegwitAddress.fromKey(BitcoinParams, key.deriveChildKey(index, true))
                    .toBech32()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedBech32Addresses, hardenedBech32Addresses)

    }

    @Test
    fun testBtcLegacyTransactionBuilder() {
        val expectedHash = "2db796f6a11825bfdad246be11bd96fb192b149621cbf53f3b8bf8a32e38aa14"
        val expectedRaw = "010000000186670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b000000006a47304402205c29c61e6720abd40aa7cf2b02841767752ce2641e6fb4d036535c3b0963f06802203b6af55f51c721a2a169ed234bc8c173f66a84d487238334f7da36a5e26bc7cb0121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b1ffffffff02905f0100000000001976a9149260e1a82c0f394549eb2f4a79f9d15ce586d4ab88ac34210000000000001976a914757af685b9736dffa7327954e60f38148f0bedd588ac00000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BitcoinParams)

        val receiverAddress = "1ELyiT6djUqaHqQKpVA48Yw3EoMnS7QD8w"
        val changeAddress = "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            BitcoinParams
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    "76a9143f6d330ab1274be7ee7ec75a387e819874c6f9c688ac",
                    100000L,
                    senderKey,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .to(receiverAddress, 90000L)
            .withFee(1500L)
            .changeTo(changeAddress)

        txBuilder.print()

        val tx = txBuilder.build()

        Assert.assertEquals(225, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
    }

    @Test
    fun testBtcSegwitTransactionBuilder() {
        val expectedHash = "54d415fc4a34e7ef008fc50e19b871fcfd27a0e3fd8d69cb161658f69a65bd8c"
        val expectedRaw = "0100000000010186670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b0000000000ffffffff02905f0100000000001600149260e1a82c0f394549eb2f4a79f9d15ce586d4ab3421000000000000160014757af685b9736dffa7327954e60f38148f0bedd50248304502210097d255ce4b0aa892d64f227179fe78fd6bb8841afbc60ee4a655ea895cdb4d9a022061d33d06edfea57488229b4118c1a3fa20b02010393fbb81211d5db3887758d40121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b100000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BitcoinParams)

        val receiverAddress = "bc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t7a9te7"
        val changeAddress = "bc1qw4a0dpdewdkllfej092wvreczj8shmw4nq64aq"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            BitcoinParams
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    "00149260e1a82c0f394549eb2f4a79f9d15ce586d4ab",
                    100000L,
                    senderKey,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .to(receiverAddress, 90000L)
            .withFee(1500L)
            .changeTo(changeAddress)

        txBuilder.print()

        val tx = txBuilder.build()

        Assert.assertEquals(223, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
    }


/*
{
    "addresses": [
        "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn",
        "19CVH9bUwpGL1EZnVUF52gMKTAgL9FA6Pi",
        "35PBEaofpUeH8VnnNSorM1QZsadrZoQp4N",
        "bc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t7a9te7",
        "bc1qw4a0dpdewdkllfej092wvreczj8shmw4nq64aq"
    ],
    "block_height": -1,
    "block_index": -1,
    "confirmations": 0,
    "double_spend": false,
    "fees": 0,
    "hash": "bbd3d4b635f3e38c057a76a3ab716826ee6082b476dcf5aeedea6869f3fcf211",
    "inputs": [
        {
            "age": 0,
            "output_index": 0,
            "prev_hash": "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            "script": "483045022100d02783b879bb5cd827b30467f7da137eada8cbf54d920c6b5e58ba9bf9cad46b02207e62c43b0ed537d216db5f206261255c6e497719f283e2a9106f2ef51300ad710121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b1",
            "script_type": "empty",
            "sequence": 4294967295
        },
        {
            "age": 0,
            "output_index": 1,
            "prev_hash": "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            "script": "1600147305fb6d23d9f629410f36572ed870b860926565",
            "script_type": "empty",
            "sequence": 4294967295
        },
        {
            "age": 0,
            "output_index": 0,
            "prev_hash": "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            "script_type": "empty",
            "sequence": 4294967295
        },
        {
            "age": 0,
            "output_index": 1,
            "prev_hash": "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            "script_type": "empty",
            "sequence": 4294967295
        }
    ],
    "outputs": [
        {
            "addresses": [
                "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"
            ],
            "script": "76a914757af685b9736dffa7327954e60f38148f0bedd588ac",
            "script_type": "pay-to-pubkey-hash",
            "value": 90000
        },
        {
            "addresses": [
                "19CVH9bUwpGL1EZnVUF52gMKTAgL9FA6Pi"
            ],
            "script": "76a91459ed2ffeec6fc9d9b28317eee5c7772d9bbd2b3c88ac",
            "script_type": "pay-to-pubkey-hash",
            "value": 89000
        },
        {
            "addresses": [
                "35PBEaofpUeH8VnnNSorM1QZsadrZoQp4N"
            ],
            "script": "a9142880c9ccd39581ea618053a558485452e8d1b80b87",
            "script_type": "pay-to-script-hash",
            "value": 100000
        },
        {
            "addresses": [
                "bc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t7a9te7"
            ],
            "script": "00149260e1a82c0f394549eb2f4a79f9d15ce586d4ab",
            "script_type": "pay-to-witness-pubkey-hash",
            "value": 80000
        },
        {
            "addresses": [
                "bc1qw4a0dpdewdkllfej092wvreczj8shmw4nq64aq"
            ],
            "script": "0014757af685b9736dffa7327954e60f38148f0bedd5",
            "script_type": "pay-to-witness-pubkey-hash",
            "value": 39500
        }
    ],
    "preference": "low",
    "received": "2023-01-26T13:03:12.631476222Z",
    "relayed_by": "54.226.193.118",
    "size": 792,
    "total": 398500,
    "ver": 1,
    "vin_sz": 4,
    "vout_sz": 5,
    "vsize": 548
}
*/
    @Test
    fun testBtcMixedTransaction() {
        val expectedHash = "bbd3d4b635f3e38c057a76a3ab716826ee6082b476dcf5aeedea6869f3fcf211"
        val expectedRaw = "0100000000010486670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b000000006b483045022100d02783b879bb5cd827b30467f7da137eada8cbf54d920c6b5e58ba9bf9cad46b02207e62c43b0ed537d216db5f206261255c6e497719f283e2a9106f2ef51300ad710121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b1ffffffff86670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b01000000171600147305fb6d23d9f629410f36572ed870b860926565ffffffff86670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b0000000000ffffffff86670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b0100000000ffffffff05905f0100000000001976a914757af685b9736dffa7327954e60f38148f0bedd588aca85b0100000000001976a91459ed2ffeec6fc9d9b28317eee5c7772d9bbd2b3c88aca08601000000000017a9142880c9ccd39581ea618053a558485452e8d1b80b8780380100000000001600149260e1a82c0f394549eb2f4a79f9d15ce586d4ab4c9a000000000000160014757af685b9736dffa7327954e60f38148f0bedd50002483045022100ba48a1ea0353031a4d790da18a4a578455b1cc2818784e4895c38b98af89635f022027390cd8f3c19d77b754cd823f35dbde104d6ce7a36dd4266618326381f76e050121035bcb3ba01d8a5ac55d5f2927b08455852602138d37a784cb4daa7c36415678d6024730440220387ac6adee4278ce41273041164ebcc2e3fe247f0223b5e9253381d0535e3f6a02204f5289749b5490318df32de8237ff50b3c0d9d93e7c63c41a9f9869bc4451aaa012102bcaae8b85a493fcfa88cd4e23dcea0bb3f2b879be97cb92249d759e7066aa8ea02483045022100b4f7095b7f5fca3f12bd530caf1cf184f1f0b9cc9f429bb68bbee297593c35a102204420e98c639c621365776c54a126a0e6c3d5ed480eee281afc9b298f44f1b7bf01210215f9081aede9321256eefed4ab2db5d264bf45a8e358ceaa570015ec51b2779700000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)

        val senderKey1 = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BitcoinParams)
        val senderKey2 = PrivateKey(key.deriveChildKey(12), BitcoinParams)
        val senderKey3 = PrivateKey(key.deriveChildKey(42), BitcoinParams)
        val senderKey4 = PrivateKey(key.deriveChildKey(ChildNumber.ONE), BitcoinParams)

        val receiverAddress1 = "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"
        val receiverAddress2 = "19CVH9bUwpGL1EZnVUF52gMKTAgL9FA6Pi"
        val receiverAddress3 = "35PBEaofpUeH8VnnNSorM1QZsadrZoQp4N"
        val receiverAddress4 = "bc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t7a9te7"
        val changeAddress = "bc1qw4a0dpdewdkllfej092wvreczj8shmw4nq64aq"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            BitcoinParams
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    ScriptPubKeyProducer().produceScript(senderKey1.publicKeyHash, ScriptType.P2PKH).hex,
                    100000L,
                    senderKey1,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    1,
                    ScriptPubKeyProducer().produceScript(senderKey2.publicKeyHash, ScriptType.P2SH).hex,
                    100000L,
                    senderKey2,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    ScriptPubKeyProducer().produceScript(senderKey3.publicKeyHash, ScriptType.P2WPKH).hex,
                    100000L,
                    senderKey3,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    1,
                    ScriptPubKeyProducer().produceScript(senderKey4.publicKeyHash, ScriptType.P2WPKH).hex,
                    100000L,
                    senderKey4,
                ),
                io.noone.androidcore.btclike.transaction.Sequence.MAX
            )
            .to(receiverAddress1, 90000L)
            .to(receiverAddress2, 89000L)
            .to(receiverAddress3, 100000L)
            .to(receiverAddress4, 80000L)
            .withFee(1500L)
            .changeTo(changeAddress)

        txBuilder.print()

        val tx = txBuilder.build()

        Assert.assertEquals(792, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
    }


    companion object {
        private val expectedAddresses = arrayOf(
            "1ELyiT6djUqaHqQKpVA48Yw3EoMnS7QD8w",
            "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn",
            "1KUu7ZvL3VUu7zuLDraoK4G9d9nTuBu48c",
            "14wndKoBt8Jzggq834JxgcWrPw7MQWaifZ",
            "1Cm6LHLZFVt36dkzJnUWbQ5uSzgbMJUarN",
            "16hwEu3iMWNzj7qXiQSgDuj5KmKybeAsVZ",
            "1PkBi68uc51Gj7V1dhVakSxuimbwVfGipV",
            "19TM7Jc8WeaR529dtYCpCgBMcYqFRtZzu8",
            "1C5fn6YzpfbHBfxRWGKGEYhN8GSDk6e1PY",
            "15X8h6NFPJUdZtVnGpJvaNLfWTopewpkF5",
            "1EyRDu5ktvZtV8uhrYz6p3M1PUdi64aAjg",
            "14DXRBEbjq8Pon7bHJvRGSVLwRLjyAtFwE",
            "1BVBshyYCgCsXLrQii7PXM8qyLaDpZzc1o",
            "1AYVM9DvPmH9WA3F3WfF7fkAeEZRPqwCsv",
            "1AKXochZQTVXXkbn5MZsCZSTUvyhevsL3R",
            "1DYbckbf46pRY7iPYSopcyNNkrUSTA6Z93",
            "1PAou71YbuqgSgGnCHWrAUrBQL6Vnvjo7w",
            "17TCk8JxSCKV8cvF1GuYHsvw97GfFGK5D8",
            "1CuX1tEi9QR83E3tdPBum9FD4jdJ3QHEC8",
            "154EEnT9UmYehbGGkg6CPUEcxZgsW7bQHL",
            "1Ew6vQdH7PcLYzvnJtYBsBqSTcxrbuH7sv",
            "12ziyyTHkCY7RbHG1bkY9owPWAP9KJrGew",
            "1MSLvU7ictD7pJg5MPKMPTchBFi7qwHksT",
            "14DA4VGFiStMz5cJzBd2YWr4TQ7ViQJUDw",
            "1CLJaVtdqa1pVjRh8wH7gRcYuuHrRD4CPM",
            "1CG48aTzGeUYY36oziXvv3zzgg5zMUM6kf",
            "16Z37p6eh47FZG9j7Na6RgANMPgBXUe6K3",
            "19CVH9bUwpGL1EZnVUF52gMKTAgL9FA6Pi",
            "1LYW1oq3uGVVERZkKR74eRpbQUFnsTHQJ5",
            "13JWso7L71CyGZo3LgK7i44XCtS4HrifAt",
            "1DrQ2uDsLHUxnFtZAJVE51mkp54jaNnBfW",
            "13nXyKu2ijRAda7m7WKnJvHixeCTBG22AQ",
            "1JLfBghpx4AAKFKEE77mzYUpAQMUKt9uKT",
            "1KNoShZuBj4GkQ2WjmhzP3XYh31hFwTxY4",
            "1DXpXx5gQ7uSLuK5dsVstcvRBRXL41YHoA",
            "1H5GEccdp6dfCDMMyS5pAiBLBCuiRqqtCK",
            "1MNeP6zLi16TWGqkAxfo9YXjuHbN9kHGTs",
            "131bfnN291Voow8yXsqA4H1SEomVMGFDfn",
            "1L9Bw9RG6q69WEfSfck3i6NpCHoSmBNBbY",
            "1JoFs6qjKYbktaGnDV7vdMe6GZQkadzmbY",
            "1N1UDwWD4Mg7wDXRHGd1qMX3xbFrYe3e4u",
            "1CrwtFtQ8btQ9LEiG21VWYjThC565qGoDB",
            "1C35Qe47418KTCQDc5GP7rpwuhyWwyspvp",
            "1JCksu33u625Pu8ixh54LHRBPWPmywd3rQ",
            "12bP5vvFUoacdDXJ47iCBmCJjVm3C1nAQ4",
            "1F6S2QnwdsWZUEh7QQxRLQ8BsXdk8cQw5S",
            "1Gg4FapMCt8VyiDajxvUDCLPGteFKXPr9P",
            "1FoNUWW3K9shJY1gy5iuh55eqVotAE3qzL",
            "1LPbU83uhKtqfXosAUV6ipGWRnAj27JbXR",
            "1L8VWWQ1Ypom95CWkkjsRdJPfsjZdTFTXo",
            "1EU5kRXRRL6zc9Jyp19eF6Px6LsioWK4QE",
            "15qxeXwDq5nDWc7jdNffQ6JGAxPeXdLq1h",
            "1DCjGnsNP7Rk6Ky8VRUm9FBiRA4eYVZGnB",
            "16GXNcck7G33ysju8sbXusSPdpyAbJuiLJ",
            "12aw3ZBGBYFXVFCg7dCjT7AALGEJp4SfW1",
            "1A7WNVryoafYJEBDs6uVFkggXPiBt7VsVK",
            "18AVCo2SvnrZdu4Pm9ayMPDSM1eoNs8NZ6",
            "1FpJz2nugzt8Mkxb3mzgr5c2F2mDyr8tUw",
            "1Fp1B9SwwABB1oo6fP6dy626Qmr7sB5XDY",
            "17QL9XrgEGzqbqCuyiSseeTNhPMPRG1mSU",
            "1X8vEDdMidzbqBLJNrA6gfWNg2YwgHQT6",
            "1KLswn6HcX62xfqJjaCgCaYpYvcRNbco1f",
            "1Q5xZT6H2PwbmGKHqDSEpXmY4BAcVFnXn9",
            "1JoHeiZYNTBD1LefhWUo43QkuPJuYTeTZC",
            "1GiiccQhZRTp6JmntFPoqy6vBKBGXvaQZu",
            "16RQ8232LpttrjKQxYNr9AWmAbQGcb1FwC",
            "1LarZeCwKi7GWvRCjHT2hwxBE4KiSpwrw3",
            "12Lcx26qzWJ8Vjc88kY4zqtxsttoGBGfay",
            "1Bt68CueWeuE5nes9ifvAyevaCE2uJ1X82",
            "1MCqPGRSGe2aoBdBgtgFoyoMXYpC9X5R6j",
            "1MLVqu7WqFbTvtazVry8qVctSCjaxnxZpR",
            "13pFkyYrXH4TLUTzQDxCPg8VwLqcuB6fpj",
            "1oW2Mwtz7sHzKnMnY54np8MqyK85UTWTD",
            "1KEw65h1uASF3xDFVmTyA9xMDwjFKwVPFL",
            "1Dfiz3WvzJaxWYLf3JzbywsW899bDzFLnt",
            "1L68KCMMLM2ojPaSivJjUSjW9w1Kxdah4c",
            "18HJdUBBzZMbp2xKUyKa1yzJyvrD2Q9xqX",
            "16pdK2xRKJFLhWt6wdFjZUmqLVGwhTF6eG",
            "173usQywWpXvydZJx1gCd8QTacqHs8a6Bh",
            "16PMgqWcgXtWPh6YFP71wNMFvDvapYwoPm",
            "16LaMHGo6Mj8XrBPrEdhuPtw2DMw9wyPBF",
            "1A85pGALKxVx8Ud3h5dLZRfnMADKLKgpSs",
            "18f2Gs3gdAuX9H7KUaU8C2tkrqWE9qciPZ",
            "1Dt5zYSeF4KGqnjAHE5RxiNJm6L1heE9uW",
            "1HLUem6QBiRNhnLc7TLQxpkpqm7rhVTUZ1",
            "1AAZDwQCpGP4ZYJGSk8Sm9jqb1xEqUg6it",
            "1PauaAnYHCoypsmAfLbV8KpJ3JgdHaSpwP",
            "168jhRtGHhm6Y6s5RoF8kkiM6AS98xH4aP",
            "1vzWs3hsJVB1ikFhXJrcieScfGt6h2wxh",
            "1JtuAAUr5Jr1G6JWbk5TkYduE7vFM1E3XF",
            "1DcMF8QQiuAoKdRUbwymfV5t1LYtm591VE",
            "1KiPwTARobAU4iQWNUx7S3AoDo1inXzrSV",
            "13XdCP9qwu1AyevjGd85xUrJSh2xYbTSpw",
            "1HwXewLFoxpndbk32EW5qkDuWswnCsxxXD",
            "14qE2USonEC61tqn5H99MMJ46TRJEURvCN",
            "1K1RnXjPDFxbkNPQsGeXh8v1mo4noZYRs",
            "1BSVVX8e3sr8tYYYUhuD8FrLCgXFuJXPgC",
            "1FQzwJPuKmkEKdNafxcr8f6vhgZu1j1ke5",
            "12WYgSe8xGy7XhgEUz76EG9SnUjUSueuma",
            "13ttuDeHQoVUfTnRfnm7tWABLpJ2PpkDd3",
            "1ER1DXxJzVjjRjtoGT4Vya4DrENVFens7b"
        )

        private val expectedHardenedAddresses = arrayOf(
            "17NUmP3sh8Mobh7Jcyxrnm9DM54mionoJ7",
            "1JoCyJnJn7AiDRaQhQEbiYnD3R6tZk2reK",
            "176M4ePod8eeFVkR1GMfnPuqHkNHfexBWz",
            "1JstgSvWUK3Y62gVmhPmJyANKP3QV94htD",
            "12W2yiVK4M9LbrGdCeT7ZVn1eewDRkhdSP",
            "16aYzhr7JFkrGL4xB8HthHaxbY1PuEQmtk",
            "16EDmsu9u74C7BvJFYE3Dz7QNhweSDXBYC",
            "1FzcYZUHAzhCjgcc6PcqzGeMo5H3fLBXra",
            "1HoXg1hqhs838tdVsdK7Ef3DmoBedqonyH",
            "1Jm2Bh2Qx5pSY3nuNXVtNzLFEJPpqD8cYp",
            "18sGoijKJeaNJezyAdog7aC68fNj7K7Lq2",
            "1GspBwSTTk4k3QBuLKQjbFka1SLqribrJf",
            "1M7amPYcAaHPbTU231RtmmarWqCyARz8HK",
            "1E2mNy4h27z5D9BebT3GyAZGn3Dp4UCcMc",
            "1D2Voz1A9LAGUCdcGRLyqEPQwVYkXt5USj",
            "12MA65fbsoFouoT4V1Q1JGKT3A3vhoDQM2",
            "173iH5zuqTRtr2SEYg13JGNT35H7dTvtFx",
            "1Nv1YeZHwf8RnyHz4BYBH9AeQjfU1uLuHr",
            "1JXpLKXQFAUXQHaTaDuXtwxkxsy22hAwVT",
            "15vMpFT8s47nquj4amyNpmqquCUgB3ttxU",
            "17YnJoocTFYxGpPBL2QqP279kYhRPRpjep",
            "1A9AxJzh4v81Hjcf4o1CUJ6eQidCr9pZZ2",
            "1NGzUL9FUduEPFXJKNTGgdJ8rHg3eBhKHp",
            "1M7VjTk1JVxdsQhRQurBUFJTEQtevMCNhu",
            "1DTBpVbxs8msNcbmCkrXp11m359tx3y4pf",
            "1Hzaaa1SZPd646s9c6SnfHRFi7RxSotbTp",
            "1GjYPM3cMPDofxAhkdPgfaMU78ieVw6zfq",
            "1Mqi4FLFfoCwfLBx7fXEvF123x1rkqBT22",
            "1MtDyVrEz7yWqaLWWPksUH2HQeEe7gXrJp",
            "15xLjyNZeiG8dYph4Un5FLtFWkb4uGYx2S",
            "1McuSAfBcXrCKMvNMEvjaeL3vNzmKSntMy",
            "17GaCmoKR7dnW45dEw8M4KmDh3gUMcBZi5",
            "1EesRYRpyEmksqVhPyoghBgbH9oqeS5gtF",
            "1B5X98Ens5NDaH7BAyFW1NSDGd8ejucvSf",
            "14o2XHFi7DQW7w7JqTDXtNPDmdHBVQoSqY",
            "13Caz4hd8ZTRFxP3KbrPZ2VVoyiBpd2vJ4",
            "1PnShiYPgpJ2cxsyU8TYvkLpWyacDSj3E2",
            "1Mtw1TgBzpPJvTkXpP8pwAJcHRGYvnAcqL",
            "1Ps8gzSMT4eXS3zwyFUxVHQ8dDLgVTnaEV",
            "1NWEiSBYnBsjxBrBAmBmb56kRgdsprGwxd",
            "1EiLaLFj6H7zknoMp9xbT7paWxVakfrcaP",
            "18i9YeTLy4wA9BfBveeYohXaZ2cmTz8TFv",
            "1LtC7AapAJeAardugvDLurbvF645MkX2UE",
            "1MNNHfQY2LxaNcWDoY6UfREuE61CGtL48y",
            "13EiphYhztMzNEpG1r4xbgRnemp5XdbkiN",
            "1677CKgYFy9xj52zyu7Vq45K93PvYRKjze",
            "1MdUKdrYxCadANgv465256iM3cpvGvUavk",
            "13d7E2BpLTLdDt6b86FxZfawdSzuj5JEMU",
            "1AGrSpMwHsMy8YQGqMHvi8KYzkGbaVgJsR",
            "1NEMGh3DaftYMH5duzsF5bAxGen1bbXKjg",
            "1Lss5Jt35EgmWWPDmemkarMedQfSoUQikd",
            "1KsqpZqtDhbMBig9yVcGtws1eAFBKoCc1Q",
            "1DpYspGoQKKtmUnwFwxjPfDioveFahh789",
            "1EPu2xgLvEqskSrKWrSbD8z3yRkmkp9u7",
            "15G9TZ7gr7FwVftmFtfmfM697SHwLSJrP6",
            "1CWLKkpKrGq9yU3RguoGneeFgadyKrjvQ7",
            "1JLggu7b7iDXNBo2Z1yh43GAaofQQ1HTqD",
            "15qQnwYp1nxXHd3ypDMsAfwxWssvKMgWu9",
            "17U8vbJqtujiMKHp9qy4w3Yw8qMtZm1KdJ",
            "1vNs2vtqys19oKZTeakWVnCEFPkS2RzWv",
            "1FsqHEE2wfrnWHcN8P3CS7yMbvJ4UD1RWR",
            "1FtJ6bzB2JnvUtFBrWajxfW8guFpQAvKSi",
            "1JvK5LehmBcV87tVLchoSujKeAQhYat84r",
            "1Fe4UNihgRymTLbXcrVH5zr7sBfL8ti5Xj",
            "16QDmRbTgyGeSPxSwtULvVW2euiLstpqac",
            "1HpukFGGJ3nZWFNdqSoDTqfg8K8wvWv4Ax",
            "1JtAxkn7PqFmgh6sLDY6Bi5qHassddHsm6",
            "1DCzdzHUezPdXwdC5oqEacnVJmrT5P9fgs",
            "12dD8bbxP2ggA2ipnyFFzBNJNieecq1dRp",
            "13PHg2X8GRvBe5uSLZN4pMU9jpopCFxPx4",
            "1EDzBEehYmDWCzcGNQBuW2xWASuKUuV1uS",
            "15TZqtufuMUGRQrjpxTTTSSdGMFDeQSamZ",
            "1AuMDYgyv6YRQjfswydCCzpkRKqLMddKpe",
            "1LC6XJRCKTUrCSF67MsEWRv2n3FEHHQyHK",
            "15TRyCYrqBiB8PTCHkASx6SHQ8HdciXjyE",
            "1rEJPpAehh983MWDEijFsoVcodxZ5XEu8",
            "13YgAttXcZpYFiK8ZxW9ujKHcYemdy1uYc",
            "1HwFYgA5eCBYzAPQcV2d9dZHD3rBDt8T1b",
            "1F4JZjefjaa1SptgSgStfDP7nmdi1w8RMg",
            "1FW4rMkEoCtNai1WqjyJ3oDivbB6RoPv7s",
            "1BoJC4R14aifYCi5sREVUKCRtoRUgPL5Q5",
            "1ovY8W26qSp5QWh4ymiVG3RYE9rKH4EGN",
            "1ki6p92vG9cib9E5cT8zLwF14dbcNwZEX",
            "1JBfHjhVYwSHga1Rtu3JNHzqWRSQnmuYri",
            "1Ga8AW5rLWF3eQbQZoeQZuvwvDAeNVAfGd",
            "136chWMozUtDqsAEVuxF6ozMGMGJ9je41V",
            "17bfwE8pkG2BJh1SmBLCVFWNdTapst5MWu",
            "1AWX1NNEYFGtFZHWvsafGYU332L9R2k9tD",
            "1L78RftGDweWEi15onKytpozFhRYQvMnbs",
            "1AUcY2X6PLxLW1xCVr28MYUu9GMaNjDQ3Q",
            "14wz9rDH6VA6gEhuZJUaTGCt3Q7cbApyeq",
            "19dUq1PjgyGjEYW2AUy2MqeZSawnZmjqA7",
            "1JCAaq4MtA7rU4Gd54bRRWErxDHMcwR613",
            "1A6Xrbg6Zk38K4wgY5LvwNmDM92NSVXALt",
            "16R5BAYQrzMfQDE3YY5ZPgP4VHGQBcKDVo",
            "1BfA9tsKEdWTSYW32cph2j9MZJqQ5qaasn",
            "1PKpzjHfmdWX7BRkkWHdwPs2aAzGwApCZg",
            "1AnkeZNJDZyT8sU25qWqw7NzjKUMGyBPku",
            "1L2p6Mkvp6BaQPaSSN9UERsVjgUYmnCaA6",
            "1Jceh4nimcJm3jqpcHQ7b9JUfU7AfyTVLS",
            "1NZ4AKNfxYbxuJW1yznCRYdkxMGWdpbuRw"
        )

        private val expectedBech32Addresses = arrayOf(
            "bc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t7a9te7",
            "bc1qw4a0dpdewdkllfej092wvreczj8shmw4nq64aq",
            "bc1qe2u070zyex2a3vuecc6w9hec6m7zrv0lgza5em",
            "bc1q9dzw6x58mcw79e2ljw8k3dahaft2ktt76q6j6f",
            "bc1qsyqzxgsuez9kgalqkjkkcscqgv2y697ykfmrse",
            "bc1q86tgkkdzzrekz4cc8puv8lj4uj3nhlzw6rke3z",
            "bc1ql97e0vxscm3nj0gzsepqtw7lkhkr276hg8vngm",
            "bc1qtj7fkmk9cz5tsl7lalah4yy8u6edslhck3ynpy",
            "bc1q0x9k44y0s7fzyecrvcdg6dhkarvgx0nau7qjse",
            "bc1qxxfkdl58fwgnznmd3z4uyn8qy5chavnr6xmdzw",
            "bc1qn9zjv8y8yyqn3s03v99a53llqe33n4e7fy2td3",
            "bc1qydrvyarz8pun0vg48pnuuzz4evj7ll28vnt36y",
            "bc1qwvzlkmfrm8mzjsg0xetjakrshpsfyet922jmvg",
            "bc1qdzkmpg5wfukg0f8ql6rysy92znlcy3r943nvz3",
            "bc1qvca92szcp0ja6vxh73dzg5f3zlz62wa7ksug0z",
            "bc1q3xd5eurx6s5wz577yhhqvts85rpcrgcdzx9a5e",
            "bc1q7vk63huqr2zshn6juc9lm92ppxncpe3qaqyyu4",
            "bc1qgmz476mvzxnzcxj9857lcldhvgk4dt7658a84l",
            "bc1qs2vprtahwmynuc53qh04rnp2zxaes9zmpaxs32",
            "bc1q937v68h09q79c3wq80l9rc9v33gc5mc7veawqh",
            "bc1qnr2s5gv8u805yycqdpkpvyzf56cw9zmg5e5vr9",
            "bc1qzh3wzue354dj9uv7wc85yjt9e9rhkla8fs037t",
            "bc1quqkckm0sr9njam6fcnvsv6uxupp6dft0walzhs",
            "bc1qyv6way2367852hf6mraqlhs62udh9zw9jyu88x",
            "bc1q038ltwlxp58c7ujjucpzfx8jf8nn5p4f0w799y",
            "bc1q0wpreqhnsm8cem20u7ncmf3wljeaare6m2pg9h",
            "bc1q8nnm2r5ddg8d0ccjc98ljn9edl3asxd2ft00jm",
            "bc1qt8kjllhvdlyanv5rzlhwt3mh9kdm62eu94zc6q",
            "bc1q6e0czxj68wspqv8qmajq32v206w72jvhw88dyd",
            "bc1qr9qyxr78s6ayawm6qgzn7d692c948c0dvlay7z",
            "bc1q3nu3ujs8znhywxcy3tdgt8yh3ztwg3wyw7se90",
            "bc1qr6xd9srha80mk9rkrlj2xmhlvn66qkzj0maxqs",
            "bc1qhcc74z53pkc30hduunp6hhrhfd0g23f06yrj2z",
            "bc1qexguf8kg9sgmp23jvwyld4d6qq95ld5zrqy26y",
            "bc1q396644dxwqk87vxf585752wfqdy2wt67ulw6wl",
            "bc1qkpgxfjfe6jez8lwcljpkk60zez9hneqh5j4hap",
            "bc1qmaa8glf7cxgl8zgg84j4889tnfhtdvclk7rrsl",
            "bc1qzcxnzpn6p74anrzvuhnlpe8lrhjhstnmqatyf6",
            "bc1q68mdsttvt7rnzw7f4vwyy5r00tf3fkrewk9rq9",
            "bc1qcvu6ufyugyxr2qeqffxhsslw224fvj2s8g5ypk",
            "bc1quec4m0ypgsfv22hqw229gf4utvwhzhnutx2yh6",
            "bc1qsgdef9qj3xmy035lqtxd63tk2u0mh9dv4wj350",
            "bc1q0yx72y2lpudlgf7s3skhj69z9wa2kwnpr0kzna",
            "bc1qhje4wnvn5uzfcq5ec6e7jrnzjkm9l65v6dxc7q",
            "bc1qz9utgsrcm2t0dv8mwt3ltd0jgpueg7gh3g5pph",
            "bc1qn2vt4ccwlp97zht48g6jgus8gzdmpygnyn0fl5",
            "bc1q40kd9jl65ztm7rprk2hppzrnlx05gxaekjdna6",
            "bc1q5ftdyvpr446khsnkm28v9a7yn65ye6gzcsx2ch",
            "bc1q6jc9q2uanpkrstmqzvmmdp7z70vzk9vzdgy4n4",
            "bc1q6823j73jk7sy884wkjxceca45x3a3q6w9fuv3d",
            "bc1qjwudfpyxtrpm6muxwzdp5up8cm79njcfyhd923",
            "bc1qx53wh54fm5zlj2h53a2nmrsyjz7nzrrgxe6g09",
            "bc1qshv47sgew9nc8jgfxy9tlk4cwekse6wjc6r5fk",
            "bc1q88yr3hay0f7kcaxxltcl5r4pq4xk5k00dlpkrk",
            "bc1qz930wkj5qw39h6yyn980kt3m0r2ewn8zyzuzfk",
            "bc1qv06z2tzs64xec8sy6ayhpzf2gf7xnufep2xzh9",
            "bc1qf62z43e6dx7s0f9gwvn79w9hsq8pt9dcq5f9cy",
            "bc1q52z99f798m6e8lyv2r8jeavcnypkwzveca779f",
            "bc1q5f6h2zkmhhjjmvcprjtq4njn6ekeur6knzeur4",
            "bc1qgcayu9wmpwkkm9j2fn06uxmwgupe7l30pluhu4",
            "bc1qqke3d95cqafz76vx7fuk8yztqj9he323cfdayn",
            "bc1qey6tryuvg3ct5pv05chegcu8fmz92c7kn6wtdm",
            "bc1ql5awlqhm4pn4tswplxwk50mvtypsp93j5l4hkt",
            "bc1qcvajh6g3y79yal4r05vdhcv8p9rvvy227x53jg",
            "bc1q43k6m38wr9rhka88h5397hjrf6knej53wykrk6",
            "bc1q8d67n9vsxdj26vqyyexwnh5wl5y63cn725ye32",
            "bc1q6mgh6579xjpqxts73mqs8vws6sjlxa34j9fxnp",
            "bc1qp6hqn9d37rgudvcsjprlequr0k7cvrjmuad4my",
            "bc1qwadtl4mr0wlh7szvvzdp7gdh6qxfnerhstvxle",
            "bc1qmk0h4eueez4hgfl4v0lcjvnqhaz349raeqwq5p",
            "bc1qmuf8aawtnvqlv3gxnnfgwy2mh49fwhwzgfcl6p",
            "bc1qrmsp7r9hy8wrx76k2pz7u76a6quluzt4qzptd0",
            "bc1qpr94e87vwuhvy2ehwp0c4krnkfqcwed7uklyw9",
            "bc1qeq2dykxzjtvaqp4zpfsdhqxdwtyppesz0fa8e3",
            "bc1q3t69et2j0u3vs35ge3pxhdjmt73fgclj4z9uge",
            "bc1q693fx2aetddzjrxhfa47a22fkdss7dyrp5l4m7",
            "bc1qfl0yq6ktflgv6rwhdrw5unkud8h0qw690wruyz",
            "bc1q8ld86mwahhqkv62mlnd2tdluk5vr82qpjxfc60",
            "bc1qgfwmswz7smt92rsxrrukqfukvnajfjlvre56gu",
            "bc1q8vfscmuemp7sc6j7220klxvsv75hqjlh3l4cfk",
            "bc1q82x9al7nf696aakk9n74g7heq2rqgm76mkcul8",
            "bc1qvsgpqg369rxv0epdhua8cq2udzz8uffraal0qc",
            "bc1q20uuqpjhk83zr95ns6r6nl8jkpt9h3fpmtemgy",
            "bc1q349wgqstda3mufdw0x9ftuj3kvf304wly7ku32",
            "bc1qkvc0767s57lr29m2mku3s0qyua7qnd0kq7v27f",
            "bc1qvjruvvptgd5lwf7hkxqn7ehk5n64kpjdz2cgtu",
            "bc1q777xpa6pzcdgns727n66zzl3lkcmrn6rx3psuc",
            "bc1q8p8jatkaeg9dwh6r6aw9wncrc0yqgxsmm9sl53",
            "bc1qpgmqa9sp7qq296vcawrr4en4j9gxwg2zznk0yt",
            "bc1qc39wg395rmldsanxxpyf3mcxwxa0f68yy789x5",
            "bc1q3fg0wuzjnfx5cd8z989pqkhdu7qm2capzh8hjd",
            "bc1qe4r80p5scngtdp7cmc3rvkamww0vsyy8h8gsce",
            "bc1qrwa0yrd2cq03d73glvxsa4c4675kt7x6fut6h9",
            "bc1qh8fpqthwtxk9pjfz28z5ylu5kr3l8ztdmdwexx",
            "bc1q9grnwu26unynvrvz43dgnx7wzulpq5auc3645q",
            "bc1qqdnasy3tmtfzanznqswc2m3j5xyu4kccz3h5sg",
            "bc1qw2phpjfzgpm5v2gd3zsqnnmjzg3cp9e2ymny6c",
            "bc1qncdmpkrnqv7ma3kjrj5admz9v9trgkxurx0eu5",
            "bc1qzz82yses658c5g9gx44sh7yy5phep2u0a7znqz",
            "bc1qrlqv5v278f0x74r2n50sej8k8vd3rcr0k4kr66",
            "bc1qjv3ucag4nyf9mvkm3xnnxk46ecp9348zzplr3h"
        )

        private val expectedHardenedBech32Addresses = arrayOf(
            "bc1qghs243e8vpc0jwaqmdly2fla07am2zqrfdcxat",
            "bc1qcvm586sylersm6hkrkjucdtn3ncr80086hnkxu",
            "bc1qgtfe2pjy5gvgn7ng7fcmaa26rw69ddndv73cml",
            "bc1qcsdp82t76t3p8u9573ssam4trx0td5zre46fgr",
            "bc1qzp6ada4n6l7400da5t8kptjz2t04kezakkgtgn",
            "bc1q85c3qj7xrkepv6cwuyfwkea8nz97xjptdlcn0u",
            "bc1q89vtxr9kpy5vxwh9n4np9x86tqlrg6xpq9y7wf",
            "bc1q53mjg0zyspl3fceqycefhs2zzd52hd77ksphx7",
            "bc1qhp8vqst6r82840w80ef7gmnl2xdqhjwl2qqasw",
            "bc1qctxkmc7p2an5yl3ze9wdp3hf8cmye2kz2ulrpl",
            "bc1q2e9d75j05yx6xnssu0hzsgwyn9dscdje0h6crz",
            "bc1q4cnpxmj78uudyzjxqu9fd65hj4g62rck276qqc",
            "bc1qmjsn9qms87f7cz9x46h0u98cyjhn3zga4uuwuv",
            "bc1q3mh330jpz7nm48yapswy22ep9fglx3dtsxqmfa",
            "bc1qs05lnzmcr969al3jglvluk6hszmk88dw5qyg7z",
            "bc1qpmyq0mgvklwaf5j2fhrw8asrq6a43yf6ppnkyj",
            "bc1qgf2qkyyfd2vjm3437t0jvdamhdekx4wwz5ktc8",
            "bc1q7psjf93urkuv92f2pct6872ddw8hlxy09fjpgm",
            "bc1qcp8zplyj68akhvhtpp2dk5v7v50dhvma7jczuy",
            "bc1qxhm7e84h2d56lq6zel8u8d3wd3f5cg6tzy3ges",
            "bc1qglfhsnq5luw8vrd5khclxmaxmt85hzjyc25cek",
            "bc1qv3zvf3q9uywruelcf6473ky39vs003m5ua4wld",
            "bc1qa9sd4ldatprmttkfajrs9z0yaaxwz4ldh7m0kn",
            "bc1qmjw0a67puy5sazr4qvv273f9y8jtj39z0dpsrt",
            "bc1q3z24kpqj4gww7gu9lwxy53gv9hwpma57erqg27",
            "bc1qhfjupfx8fs0sgkj28yenacz4hm67rxh3d5gqlg",
            "bc1q4j2cue463kltyk7du38qzht74cgdkqxerzvx3f",
            "bc1qujvvpm6vvdh2zvne0yppqcchls0ah2xf8zu0cv",
            "bc1qu5fg7kffk3uwuawzvzzwtf4kvad3way7thy3c3",
            "bc1qxetacg5mzlx95gkzkt97j5pncvuvaq8k8unlxl",
            "bc1qugkdsrd8phksy3z9yjcwrn96p32y36ruvy8x2l",
            "bc1qgnptt6par8ekjjf4fwhk6sej3h9umegly93kqz",
            "bc1qjhp3mmrnxnd4qd6elem025hy90rpw8f3ta59ux",
            "bc1qd6xpf5lwkfxqjyfjnh9wsdfh8epmdaml4u6hqe",
            "bc1q9xwvswmr3rgessundw79z37vqxnamcddk7nxvs",
            "bc1qrqsnyzy6cjalhdv6sruqrfld4sdmfpenrjmxq3",
            "bc1ql840pscfhq6fm0w7ka8k3q32ky46dc5yjeywhz",
            "bc1qu56dqvka77d68m4n8jhelcwgvmuads0emqz7ve",
            "bc1qltxlhaqlsnxmxry2atd0kq2726z33f9xdsr434",
            "bc1qa03zwr8dz70apvccpetwg65qaf8e6qkrwh5pl9",
            "bc1qje4s08d37ne396ek3m86pu7l86znjwexp9d6at",
            "bc1q2jg3rp272kgngh0pamq4e9zspn48detax479vx",
            "bc1qmgvwqqxh07p28fpvnpd4k6tc36g86pawhgvf3c",
            "bc1qmaks230jjvhk3ly9z2vs9fcyk9qnea223fd0cx",
            "bc1qrzyfy93xvgmewa5zddfemvm8zzweqmf7ndnr4e",
            "bc1q8qqyh5cs8dyc3nx0dz7v62tkuguefyte3sz4ty",
            "bc1qufyycpnt2xwahprctyrpa5jg8j2n8dxz82vana",
            "bc1qrnzxcygtw43lx0ev6q9jnzvznlh0mjy5693564",
            "bc1qvku22mputu5ga9k5kccf5g69zhe4yf0u3sjssk",
            "bc1qars0sn3tvzetthgzz2qyeyn6c4cedjpx067smg",
            "bc1qmgy0eesj7eugrfn9n2p8kmvylnp8druru7wfaz",
            "bc1qeu8aptc0r7j2ealzgce07tfnyd5wpvexps7wct",
            "bc1q3j06k8l8dgmkdmsgntyg5vl4lsjjsempcuv3sn",
            "bc1qq2yg2e978ja0prafkqdl9muqxdzqk9f7yf6ahv",
            "bc1q967umpqkqr5feszd27xeykqfx778dxdc3e34gu",
            "bc1q0c6e8tr5hyynnannsl4f3r8zkrsxjun95tkazw",
            "bc1qhcejhcrv5ldvz0f5pu0s5mdxpr38vlfndaw55r",
            "bc1qx5y9gase0t2w059sdc7ujf2vc52lnf9mjmwcfz",
            "bc1qgmefntzctvt89rde4pw0zfguxpkcuxvw4ehs7k",
            "bc1qpgvyh306uryyktrxxqukd5k5ryd87k4fjx6wxq",
            "bc1q5vhdc6k9cx7kmfw58s32s4rdnm6fz0dxutdylx",
            "bc1q5dzn78shfxk9luau6w50nc7e3ptv6cx402f48z",
            "bc1qcj85twehy24gdyyeswvz6sjv52ykffxc80afsr",
            "bc1q5z2qesxg5pwr0y5j4n58cvefvk2srlwzhua8sd",
            "bc1q8v7d50d9vnfnfqz9v44c98c7d3a6f7hz5zxu9t",
            "bc1qhzge0v4d5rzsncflvs4hjguuw2ft757x9w602m",
            "bc1qcsn6465gq6932vgzlgl6mgqtr4nhdu3fucr0d9",
            "bc1qshnr95k0gkhtcplhmdrc55lw27lcluz4cqe6f7",
            "bc1qz8gn5y06h7dr3930qnfgu0hcnj2s808fume8cn",
            "bc1qrgn4yr597cl9sha66ugznv0v8qm6ryvkjk7672",
            "bc1qjy894u2u9juw7fhfq2uv4vl34nlpyc3r67mpay",
            "bc1qxrnthm4h6ttjzn90e2s2048wyecgs9uk93nsn7",
            "bc1qdj06x3kqc8rag76mgrr9qjrqer03wm6pk9rcp7",
            "bc1q62pexjnst9y72e9eeachcqhxj4tqac6muca7a9",
            "bc1qxrszjs5wt2pkxamf0kc40s0ydhqyhk7uwfxdju",
            "bc1qp98hh7lgult9amlra3c4ujkwkkquhra7n3tqda",
            "bc1qr0kasf6l93tl6pnvsazjvmk6yaqe3nkka8t8d4",
            "bc1qh8zfmudf7kftx0f6yrrggp69ehcz8k4sp8jcz8",
            "bc1qngc6h0hurj5ywkhcj7lkz5t552uf2shl2szfrz",
            "bc1qnugsnkl7wvn5fjtgwqhupccuzg7uj6tknahyw4",
            "bc1qweetmlp9ec9gm47pplulej86sfyyf8mgawk6xa",
            "bc1qpr0axrnjttx5gzmkmudz7s0xvj0gf54weqrja5",
            "bc1qppzrruvnex6w995pnkyurlf4a56r6uzm5ha89z",
            "bc1qh3lyyajncvq09xgzhkpjqxx9rlfxrs8x2s6yux",
            "bc1q4txe0uhpz68w4raahttxyn8657jlq252ffkr4y",
            "bc1qzuqzrqdka8aggxx950vwu75t9fxh5elk6rs00p",
            "bc1qfp0kvr3dhqn3cjet66r4mkcffyttnc2l0ynwlw",
            "bc1qdp8rudzlp99czquyw9t5q2823yemz8eaqjlmgs",
            "bc1q6xf3tq6za9kj24udj5svx2x82wutm2qk56ur24",
            "bc1qvleqdt0s07pfmk488z8scasma48tsem423u79l",
            "bc1q9d8gc0mx30646ql6e3r24n55yf4nu7ey3zprhq",
            "bc1qt6nnt4dzdhqcdl2jmg2d6cv986y9lgklfp3usp",
            "bc1qhjttdtnj4zy0k3jm6q4ln25fll362feactvwt6",
            "bc1qv0z006e3etdp5ylnxh5nszj2p2u0mttzprcmd8",
            "bc1q8dnp36jdyr5de4jexqk4kr4jnet02tf6ppd9px",
            "bc1qwn5tfzgmtwccwvqjguk7aqj3njgnqyzlherpvv",
            "bc1q7n39rtypn0v2l64ewysm3vrs52rcj7ha0uwgz4",
            "bc1qddsyj987ll76gckmwkdsefzv92a52hl23lrrf4",
            "bc1q6rppmkxlu5wf666hg8gc0twmltd43dupnkepgj",
            "bc1qcyuz3hlft7k2njdjw8fdhdj6q7dhjlhs592eh3",
            "bc1qa34fwvdruncjqaqua5ye5jtvx2hw9pk86049zq"
        )
    }

}