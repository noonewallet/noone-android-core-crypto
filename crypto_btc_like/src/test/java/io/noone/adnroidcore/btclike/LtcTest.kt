package io.noone.adnroidcore.btclike

import io.noone.androidcore.hd.ChildNumber
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.utils.hex
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.addresses.LegacyAddress
import io.noone.androidcore.btclike.addresses.SegwitAddress
import io.noone.androidcore.btclike.networks.LitecoinParams
import io.noone.androidcore.btclike.transaction.*
import org.junit.Assert
import org.junit.Test

class LtcTest {

    private val seed = "c4eb38a2e9a353c180e8e74c474e9192633f81afdbaa49d0c6207def597dafc1".hex

    @Test
    fun testAddressesGeneration() {
        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val addresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(LitecoinParams, key.deriveChildKey(index)).toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedAddresses, addresses)

        val hardenedAddresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(LitecoinParams, key.deriveChildKey(index, true))
                    .toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedAddresses, hardenedAddresses)

        val bech32Addresses = (0..100)
            .map { index ->
                SegwitAddress.fromKey(LitecoinParams, key.deriveChildKey(index)).toBech32()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedBech32Addresses, bech32Addresses)

        val hardenedBech32Addresses = (0..100)
            .map { index ->
                SegwitAddress.fromKey(LitecoinParams, key.deriveChildKey(index, true))
                    .toBech32()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedBech32Addresses, hardenedBech32Addresses)

    }

    @Test
    fun testAddressedEncoding() {
        val expectedAddress = "ltc1q5rkj6vydjmf7f4y06egua2yhvuktqry60gjem0"
        val expectedLegacyAddress = "LZtrWDgnE8XFmwDvg9FnKiFc2R6AE8uZXp"

        val seed = "3929e068896221bd59b37cc5cac18982862e155381cba62d03513712e9286396a27da9d965d6123eb16b4eef82c4ddcdfd748898b71841bb7342a3c255ca0f58".hex
        val ltcPath = "84'/2'/0'/0/0"

        val addressKey = DeterministicSeed(seed).getKeyFromSeed(ltcPath)

        val address = SegwitAddress.fromKey(
            LitecoinParams,
            addressKey
        )

        val legacyAddress = LegacyAddress.fromKey(
            LitecoinParams,
            addressKey
        )

        Assert.assertEquals(expectedAddress, address.toBech32())
        Assert.assertEquals(expectedLegacyAddress, legacyAddress.toBase58())
    }

    @Test
    fun testLtcSegwitTransactionBuilder() {
        val expectedHash = "df5e1a23108a4a66927ff0c307a4040bb5873a21c4c8b2f8ab80d078cc008416"
        val expectedRaw = "0100000000010186670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b00000000000000000002905f0100000000001600149260e1a82c0f394549eb2f4a79f9d15ce586d4ab34210000000000001600149260e1a82c0f394549eb2f4a79f9d15ce586d4ab02483045022100c0ed097f4c83919abb152bd10af8938afd6c74bed6931bf1125718696dc3a599022078cdc4dabd9e8ba0fc2fb6d481f2903bb99d63203f5d511046bf222db4b8a1c20121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b100000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), LitecoinParams)

        val receiverAddress = "ltc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t6pl0pw"
        val changeAddress = "ltc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t6pl0pw"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            LitecoinParams,
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    ScriptPubKeyProducer().produceScript(senderKey.publicKeyHash, ScriptType.P2WPKH).hex,
                    100000L,
                    senderKey,
                ),
                sequence = io.noone.androidcore.btclike.transaction.Sequence.ZERO
            )
            .to(receiverAddress, 90000L)
            .withFee(1500L)
            .changeTo(changeAddress)

        val tx = txBuilder.build()

        tx.splitTransaction.print()
        tx.rawTransaction.print()

        Assert.assertEquals(223, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
    }

    @Test
    fun testLtcLegacyTransactionBuilder() {
        val expectedHash = "8b692d0cb1bd57533b613c689a0f654697d7929114c1c5a6ce96590b9b53b3f7"
        val expectedRaw = "010000000186670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b000000006b483045022100a01817075b9f0d7ad9b53ade48afa56585973f3638bc7fb0383b91fed890c0bb022049856e26c2f4493f74ac034f75efea268b03fa79ca7d1f560427df29b0e271d40121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b10000000002905f0100000000001976a914a0ed2d308d96d3e4d48fd651cea897672cb00c9a88ac34210000000000001976a914a0ed2d308d96d3e4d48fd651cea897672cb00c9a88ac00000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), LitecoinParams)

        val receiverAddress = "LZtrWDgnE8XFmwDvg9FnKiFc2R6AE8uZXp"
        val changeAddress = "LZtrWDgnE8XFmwDvg9FnKiFc2R6AE8uZXp"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            LitecoinParams,
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    "76a9143f6d330ab1274be7ee7ec75a387e819874c6f9c688ac",
                    100000L,
                    senderKey,
                ),
                sequence = io.noone.androidcore.btclike.transaction.Sequence.ZERO
            )
            .to(receiverAddress, 90000L)
            .withFee(1500L)
            .changeTo(changeAddress)

        val tx = txBuilder.build()

        tx.splitTransaction.print()

        Assert.assertEquals(226, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
    }

    companion object {
        private val expectedAddresses = arrayOf(
            "LYZvyfQTp95dYe6Uzd9MQZzoT1j4X6gYzR",
            "LVw8dGtDQa4vaheqH2wGrJBn9wgnHzzof4",
            "LdhrNnEA89ixNobVPza6b5KuqN9jzzFFGj",
            "LPAjtY71xnZ3wVXHDCJFxdacc9UdWzby72",
            "LWz3bVePLA86MST9UvTosR9ffD3sWE3d6B",
            "LQvtW7MYSAd3yvXgtYRyVvnqXyhFdqFrgc",
            "Lhy8yJSjgjFKyvBAoqUt2U2fvyyDfH2Ati",
            "LTgJNWuxbJpUKpqo4gC7UhF7pmCXZmhv6G",
            "LWJd3JrpuKqLSUeagQJZWZm8LUoVtSfUuG",
            "LPk5xJg5TxigphBwSxJDrPQRigB6ork9B6",
            "LZCNV7Payaowjwbs2gyQ64QmbgzzAiLbf7",
            "LNSUgPYRpVNT4aokTSuiYTZ79di28EUc3t",
            "LVi98vHNHLSvn9YZtr6goNCcBYwVsjKxAK",
            "LUmScMXkURXCkxjQDeeYPgovrSvhRSjRpM",
            "LUYV4q1PV7janZHwFVZAUaWDh9Lyp85tXm",
            "LXmYsxuV8m4UnvQYiao7tzS8y4qiaLtSWL",
            "LhPmAKKNga5jhUxwNRW9SVuwcYTmyszAMW",
            "LRgA1LcnWrZYPRcQBQtqZtzhMKdwQCzsP2",
            "LX8UH6YYE4fBJ2k3oXBD3AJyGwzaAwcdRu",
            "LPHBVzkyZRnhxPxRvp5VfVJPAn49dnkCwH",
            "LZA4Bcw7C3rPoocwV2XV9CuCfqL8fKWAFX",
            "LMDgFBm7prnAgPyRBjjqRq19iNkRNtRaN6",
            "LffJBgRYhYTB57NEXXJefUgTPU5PzoFvSj",
            "LNS7Kha5o78REtJUAKcKpXupfcUmsZBXyD",
            "LWZFqiCTvEFskY7rK5GQxSgK87f8XqsbMC",
            "LWV1PnmpMJibnqnyArXEC54kttTGTWUqGp",
            "LQmzP2QUmiMJp4qtHWZPhhE8Zc3Thr8xQm",
            "LTRSYMuK2UWPG3FwfcENJhR5fP3cJEZFSK",
            "LemTH28syvjYVEFuVZ6MvStMcgd534jUxq",
            "LMXU91RABfT2XNVCWpJQz58HR6oLP8sWt3",
            "LY5MJ7XhQwj234aiLSUXM2qX2HS1gecHHi",
            "LN1VEYCroPfDtNovHeK5awMVArZjN9mMcD",
            "LcZcSu1f2iQDa41PQF75GZYaNcikYfJ2H4",
            "LdbkhusjGPJL1CifuuhHf4bJuFNyNkDSeS",
            "LXkmoAPWUn9Vbi1Ep1VBAdzBPdtc7FArib",
            "LbJDVpvTtksiT23X9a57SjF6PRGzXCN9Ar",
            "LfbbeKJAnfLWm5XuM6f6RZbW7VxeJJnFzh",
            "LMEYvzfrDfjs4jq8i1pTLJ5CT28mVTsSAi",
            "LeN9CMj6BVLCm3MbqkjLz7SaQWAiufYZu5",
            "Ld2D8K9ZQCqp9NxwPd7DuNhrUmn2hxf2uV",
            "LgERV9p391vBC2DaTQcK7NapAod8jx2vXF",
            "LX5u9UCEDG8TQ8vsS9znnZoDuQSNC2GQA2",
            "LWG2frMw8fNNi16NnDFgPsti7vLo6qgHmP",
            "LcRi97LsykG8ehpt8q4McJUwbim4698An2",
            "LLpLM9E5ZTpft2DTEFhVTnG4wi8KPTyvPa",
            "LZKPHd6miXkcj3PGaYwicRBx5k12HrundD",
            "Lau1Wo8BHYNZEWujv6umVDQ9V71XU2wEJf",
            "La2KjiosPp7kZLhr9DiCy69R3iBAEWLqqT",
            "LecYjLMjmz8tvLW2LcUPzqLGdzY1BavrSx",
            "LeMSmihqdV3pPstfvtjAheN9t66qp3tjes",
            "LYh31dqFVzM3rx18z98wX7TiJZEzu6mv2J",
            "LQ4uukF3uk2GmQotoWexg7N2PAkvfiKbAm",
            "LXRgY1BCTmfoM8fHfZU4RGFUdNRvkotuWg",
            "LQVUdpvaBvH7EgS4K1aqBtW9r3LSfJSK5J",
            "LLotJmV6GCVak3tqHmC2j8DvYUbazwgs84",
            "LULTdiAotEubZ2sP3EtnXmkSjc5U3o43St",
            "LSPSU1LH1T6cthkYwHaGdQHCZE25QF4NaL",
            "La3GFF6jmf8BcZekDuyz86fnTF8W9zsphe",
            "La2xSMkn1pREGcVFqX5wF75rczDPzKGiEs",
            "LRdHQkAWJwEtrdu59rSAvfX8ubifaDDb4Y",
            "LKk6BSXTSNt3rdsVUWqTNhjGatPq4vTxHE",
            "LdZqCzQ7hBL6DUXTuiByUbcam8yhYeRLzV",
            "LiJupfQ774Bf251T1MRY6YqJGPXtayfZSG",
            "Ld2EuvsNT7RGG9LpseU6L4UX7bgBmdJn1f",
            "LawfspiXe5hsM7Tx4PP77zAgPXYYdzWaEr",
            "LQeMPELrRV8x7Y1a8gN9RBaXNomYhbKaML",
            "LeooprWmQNMKmj7MuRSKyy1wSGgzYxukqT",
            "LLZaDEQg5AYBkYJHJtXNGrxj67G5RXmuGK",
            "LW73PRDUbK9HLbM2KrfDSzignQbK2gT36s",
            "LfRneUjGMJGe3zKLs2fZ5zs7jmBUJYMydx",
            "LfZT77RLuuqXBhH9fzxS7WgeeR6s5aMear",
            "LN3D2BrgbwJWbHA9aMwVfhCG9ZCtxPMzyD",
            "LL2THaFj4n7MF8UWxg4N4qC84BgQ8S63PV",
            "LdTtMHzqypgJJkuQfuTGSB27SA6XQneKoB",
            "LXtgFFpm4xq1mM2pDSyuFxwGLMWsRJtmuv",
            "LeK5aQfBR1GrzCGbu4J2kToGN9Nc1WEix7",
            "LSWFtgV25Dbf4qeUf7JsJ145C9DV8YmbBE",
            "LR3aaFGFPxVPxKaG7mF2qVqbYheDqGd2XA",
            "LRGs8dHmbUmzESFU89fVu9UDnqCa465eB4",
            "LQcJx3pSmC8ZeVnhRX6KDPR28SHruAZfDK",
            "LQZXcVadB1yBnesZ2Nd1BQxhERjDHXWxmx",
            "LUM35UUAQck1PHKCsDcdqSjYZNabQg6mW9",
            "LSsyY5MWhq9aQ5oUeiTRU3xX53sWFTvibU",
            "LY73FkkUKiZL6bRKTN4jEjS4yJhHoae44b",
            "LbZRuyQEGNfRxb2mHbKiEqpb3yV8onwTT4",
            "LUPWV9i2tvd7pLzRct7k3AoboEKX1bQNY5",
            "LhorqP6NMs435gTKqUanQLt4FX3uRiJiFV",
            "LQMgxeC6NN19nuZEbwES2mn7JNoRGwdoG4",
            "LL9wn5MXwxjEGXSQsfJ9tjiCpseAEziWbg",
            "Ld7rRNng9y64Wtzfmt4m2ZhfSLHXQ45buX",
            "LXqJWLiEoZQraS7dn5y4wW9eDYvAsmLR4q",
            "LdwMCfUFtFQXKX6fYcwQi4EZS1Nzw9G3ut",
            "LMkaTbTg2ZFEETctSm7PEVv4euQEhnJXac",
            "LcAUv9e5td4qtQSCCNVP7mHfj6K4Lr3oiN",
            "LP4BHgkdrtS9GhXwFR8SdNMpJfnaHqAFE8",
            "LKXxgzqZTsW1rZ4Yb1FwoiCgDzALsXrUCD",
            "LVfSkjSU8Y6C9MEheqtWQGv6QttXzDTUnS",
            "LZdxCWhjQRzHaS4jr6c9QgAgutwB9GvfKj",
            "LLjVwewy2wDAnWNPf86PWHDCzh6kZVM7np",
            "LN7rARx7VTjXvGUaqvkRAXDwZ2fJZUhG7o",
            "LYdxUkG959yngYaxSb3oFb7z4SjmQdVAUo"
        )

        private val expectedHardenedAddresses = arrayOf(
            "LRbS2bMhmnbrrVoTo7xA4nCyZHS3pyN8yU",
            "Ld2AEX68rmQmUEGZsYDtzZqyFdUAhTwfoP",
            "LRKJKrhdhnthWJSaBQLy4QybVxjZk1Qc9F",
            "Ld6qwfELYyHbLqNewqP4azE8XbQgh5Vk7Z",
            "LLizEvo991PPrexnNnSQqWqmrsJVV881ix",
            "LQoWFv9wNuzuX8m7MGHByJeiokNfyACEfi",
            "LQTB36CyymJFMzcTRgDLW1BAavJvY2KQpH",
            "LaDZomn7FewFzVJmGXc9GHi81HeKp6uAy5",
            "Lc2UwE1fnXN6PhKf3mJQWg6yz1YvqBDEeL",
            "LcyySuLF2k4VnrV4YfVBf1Q1SWm6x8Y382",
            "LT6E4w39PJpRZTh8LmnyPbFrLsk1FcSgd8",
            "Lb6mT9kHYQJoJCt4WTQ2sGpLDei84Hcptd",
            "LfLY2brSFEXSrGABD9RC3necj3aFELMcxi",
            "LYFieBNX6nE8Twsomb2aFBd2zFb67JV3uk",
            "LXFT5CJzDzQKj1KmSZLH7FTB9hv2gmaaov",
            "LLa7MHyRxTVsAc9Df9PJaHPDFNRCqPenbL",
            "LRGfYJJjv7fx6q8PiozLaHSDFHePhxFM3Q",
            "Lh8xors82KNV3mz9EKXUZAEQcx2k8jepp1",
            "LckmbXqEKpiaf6GckMtqAy2XB6LJ7iuCsW",
            "LQ9K5TkxwiMr6iRDkuxg6nuc7QqxJYgTNE",
            "LRmja27SXuo1Xd5LWAQ8f3Auxm4hXQJS3x",
            "LUN8DXJX9aN4YYJpEvzVkKAQcvzV2DyRKY",
            "LgVwjYT5ZJ9He4DTVWSZxeMu4W3KhtfFSD",
            "LfLSzg3qPACh8DPab3qUkGNDSdFw7fruih",
            "LXg95hunwo1vdRHvNtqq625XFHXB3CgL77",
            "LcDXqnKGe3s9JuZJnES5wJV1vKoEYnp5h9",
            "LaxVeZMSS3TrvkrrvmNywbREKM5vex7WUX",
            "Lg4fKTe5kTSzv8t7HoWYCG4nGAP8nUFcXr",
            "Lg7BEiA54nDa6P2fgXkAkJ63crbvEkD873",
            "LQBJ1BgPjNWBtMWrEcmNXMx1ixxM43Hjdt",
            "LfqrhNy1hC6FaAcXXNv2rfPp8bN3Q7csiE",
            "LRVXTz79VmsqkrmnR57eLLpyuG3kZ1PXr2",
            "LYspgkjf3u1p8eBra7nyyCkMVNB7kS5pe4",
            "LVJUQLYcwjcGq5oLM7EoHPVyUqVvtFbadt",
            "LP1ynVZYBseZNjoU1bCqAPSyyqeTcWnu9v",
            "LMRYFH1TDDhUWm5CVjqgq3ZG2C5U13Yk61",
            "Li1PxvrDmUY5sma8eGSrCmQajBwtRAHNoJ",
            "Lg7tGfz25UdNBGSgzX88DBNNVddq2xZDJh",
            "Li65xCkBXitagrh79PUFmJTtqRhxY9R8Fq",
            "LgjByeVNrr7oCzYLLuB4s6AWdu19yKpWFN",
            "LYwHqYZZAwN41bVWzHwtj8tLjArrtvdEGh",
            "LSw6ormB3jBDPzMM6ndr5ibLmEz3ZK2cue",
            "Lf79NNteExtDqfL4s4CeBsfgTJRMUiD92V",
            "LfbKYsiN71CddRCNyg5mwSJfSJNURm8sGW",
            "LMTg5urY5Yc3d3WRBz4FshVYrzBMfhS4C7",
            "LQL4TXzNLdQ1ysjAA36o7595MFmChG6L9f",
            "LfrRarAP2rpgRBP5EE4KM7n7FqCCRFL8SM",
            "LMr4VEVeR7agUgnkJEFFqgehqfNBonWVtA",
            "LUVoi2fmNXc2PM6S1VHDz9PKCxdsktaov6",
            "LgTJXuM3fL8bc5mo68rYMcEiUs9HfNQzNs",
            "Lf6pLXBs9tvpmK5Nwnm3rsRQqd2irFmjpq",
            "Le6o5n9iJMqQSXNK9dbaAxvmrNcTTywzkU",
            "LY3W92adUyZx2HV6S5x2fgHV291XfSXWCV",
            "LKTMAFGWRaUu8Z91VeqjsECkGBo2tqY4Pb",
            "LPV6imRWvmVzkUavS2f4wN9uKefDR3jEF1",
            "LWjHay89vw5DEGjas3na4fi1to1FQ3Qqig",
            "LcZdx7RRCNTaczVBj9xzL4Kvo22gW1JgGT",
            "LQ4N49re6TCaYRk8zMMASh1ij6FCUeoQ2h",
            "LRh6BocfyZymc7yyKyxND4chM3jAiDWjrC",
            "LL9L8FEive74Qc1idna3nWqxSTm2Xhqw5w",
            "La6nYSXs2L6qm6JXJX2Vi937p8fLeiYoZx",
            "La7FMpJ16y2yjgwM2ea3EgZtu7d6ZEJqKo",
            "Ld9GLYxXqqrYNvaeWkh6ivo5rNmygxfAU3",
            "LZs1jb2Xm6Dpi9HgnzUaN1ut5Q2cEVjfg7",
            "LQdB2duHmdWhhCec82TeCWZns85cy6ehnv",
            "Lc3s1Ta6Ni2cm44o1anWjrjSLXWE5S4FGV",
            "Ld78Dy5wUVVpwVo2WMXPTj9bVoF9nQDYsX",
            "LXRwuCbJjedgnkKMFwpXrdrFWzDjDFToWT",
            "LLrAPounTgvjQqQyy7EZGCS4aw1vhahpQ7",
            "LMcEwEpxM6AEttbbWhMN6NXux3B6GL5k6q",
            "LYSwSSxXdRTZToJRYYBCn42GNfGbdTnwGX",
            "LPgX77DVz1iKgDYu16SkjTWPUZcVjsJtMJ",
            "LV8JUkzozknUfYN387cVV1tWdYCcV2FAuA",
            "LeR3nWj2Q7iuTEwFHVrXnSynzFcWVKWDML",
            "LPgPEQrguqxEPC9MTt9kE7W3cLeumi8zhj",
            "LL5BZc7zjMwCNr3fPNi2XtsFq21EgiFiq1",
            "LMmdS7CMhE4bWX1Hk6VTBkP3pm23kzyGHL",
            "LcACotTuirRcEy5Znd1vRed3RGDTHugy4d",
            "LZHFpwxVpEp4hdaqcpSBwESszyzz7Lis9c",
            "LZj27a44ss8RqWhg1sxbKpHV8oYNWiiwKT",
            "LW2FTGiq9Exio1QF3ZDnkLGC71nksAWxfz",
            "LL2soLorBVgsLDCrF7m1mH7BkSX8XB5qY7",
            "LKyfN2SrzvPfyPqPFkSSGN11DGzskqSSRe",
            "LcQcYx1KdbgLwNhb532beK4bidogwpAyo8",
            "Lao5RiPgRAV6uDHZjwdhqvzi8RXvRfqw8F",
            "LMKZxife598H6frPg3wYNq47UZdaFJ1KS8",
            "LRpdCSSepvGEZVhbwKKVmGa8qfx718PH3e",
            "LUjUGag4cuWwWMyg71ZxYZXoFEhRbXLmaK",
            "LeL5gtC6JbtZVWhEyvKHAqskTunpZaixYL",
            "LUhZoEpvU1CPkpeMfz1RdZYfMUirVFtoGW",
            "LPAwR4X7B9Q9w3Q4jSTsjHGeFcUtiCdsbX",
            "LTrS6DhZmdWnVMCBLcxKdriKeoK4kfKD15",
            "LcR7r3NBxpMuirxnFCaihXJdARedqSuq6F",
            "LUKV7oyveQHBZsdqiDLEDPpyZMPeegTo31",
            "LQe2SNrEwebif1vCig4rfhSphVdgKE1RSD",
            "LVt7R7B9KHkWhMCCCkozJkD7mXCgA47zLE",
            "LhYnFwbVrHkaMz7uveGwDQvnnPMZ6rd8HL",
            "LV1humg8JEDWPgABFyW9D8SkwXqdSp89rR",
            "LeFmMa4ktkRdfCGbcW8mWSwFwtqpoEfSpk",
            "LcqbxH6YrGYpJYXynRPQsANEsgUSmeHEQ3",
            "Lgn1RXgW3Cr2A7CBA8mVhZhXAZdnoDh4N6"
        )

        private val expectedBech32Addresses = arrayOf(
            "ltc1qjfswr2pvpuu52j0t9a98n7w3tnjcd49t6pl0pw",
            "ltc1qw4a0dpdewdkllfej092wvreczj8shmw4huq39s",
            "ltc1qe2u070zyex2a3vuecc6w9hec6m7zrv0lv78spt",
            "ltc1q9dzw6x58mcw79e2ljw8k3dahaft2ktt77uqkze",
            "ltc1qsyqzxgsuez9kgalqkjkkcscqgv2y697yj4p8gf",
            "ltc1q86tgkkdzzrekz4cc8puv8lj4uj3nhlzw7lvafj",
            "ltc1ql97e0vxscm3nj0gzsepqtw7lkhkr276hvmkhst",
            "ltc1qtj7fkmk9cz5tsl7lalah4yy8u6edslhcjd7he5",
            "ltc1q0x9k44y0s7fzyecrvcdg6dhkarvgx0nacz6kgf",
            "ltc1qxxfkdl58fwgnznmd3z4uyn8qy5chavnr76pf67",
            "ltc1qn9zjv8y8yyqn3s03v99a53llqe33n4e7dcs04p",
            "ltc1qydrvyarz8pun0vg48pnuuzz4evj7ll28g034z5",
            "ltc1qwvzlkmfrm8mzjsg0xetjakrshpsfyet9wkgl5c",
            "ltc1qdzkmpg5wfukg0f8ql6rysy92znlcy3r93dfg6p",
            "ltc1qvca92szcp0ja6vxh73dzg5f3zlz62wa7jvxvhj",
            "ltc1q3xd5eurx6s5wz577yhhqvts85rpcrgcdx6levf",
            "ltc1q7vk63huqr2zshn6juc9lm92ppxncpe3qeu7qy9",
            "ltc1qgmz476mvzxnzcxj9857lcldhvgk4dt76sm8rd0",
            "ltc1qs2vprtahwmynuc53qh04rnp2zxaes9zm9pu5f6",
            "ltc1q937v68h09q79c3wq80l9rc9v33gc5mc7g982c8",
            "ltc1qnr2s5gv8u805yycqdpkpvyzf56cw9zmgs9wgm4",
            "ltc1qzh3wzue354dj9uv7wc85yjt9e9rhkla8dv44xm",
            "ltc1quqkckm0sr9njam6fcnvsv6uxupp6dft02p9x0q",
            "ltc1qyv6way2367852hf6mraqlhs62udh9zw9kcxrlk",
            "ltc1q038ltwlxp58c7ujjucpzfx8jf8nn5p4ftjypa5",
            "ltc1q0wpreqhnsm8cem20u7ncmf3wljeaare6lkmva8",
            "ltc1q8nnm2r5ddg8d0ccjc98ljn9edl3asxd2dh4t2t",
            "ltc1qt8kjllhvdlyanv5rzlhwt3mh9kdm62eupfcuzs",
            "ltc1q6e0czxj68wspqv8qmajq32v206w72jvh2mafua",
            "ltc1qr9qyxr78s6ayawm6qgzn7d692c948c0dgr8qxj",
            "ltc1q3nu3ujs8znhywxcy3tdgt8yh3ztwg3wy2z2aal",
            "ltc1qr6xd9srha80mk9rkrlj2xmhlvn66qkzjt88zcq",
            "ltc1qhcc74z53pkc30hduunp6hhrhfd0g23f07cekjj",
            "ltc1qexguf8kg9sgmp23jvwyld4d6qq95ld5z8u7wz5",
            "ltc1q396644dxwqk87vxf585752wfqdy2wt67cr57k0",
            "ltc1qkpgxfjfe6jez8lwcljpkk60zez9hneqhsw0n93",
            "ltc1qmaa8glf7cxgl8zgg84j4889tnfhtdvcljze8g0",
            "ltc1qzcxnzpn6p74anrzvuhnlpe8lrhjhstnmyp3q32",
            "ltc1q68mdsttvt7rnzw7f4vwyy5r00tf3fkre22l8c4",
            "ltc1qcvu6ufyugyxr2qeqffxhsslw224fvj2sr5wqex",
            "ltc1quec4m0ypgsfv22hqw229gf4utvwhzhnu06sq02",
            "ltc1qsgdef9qj3xmy035lqtxd63tk2u0mh9dv3jg4vl",
            "ltc1q0yx72y2lpudlgf7s3skhj69z9wa2kwnp8nvxtd",
            "ltc1qhje4wnvn5uzfcq5ec6e7jrnzjkm9l65v73uuxs",
            "ltc1qz9utgsrcm2t0dv8mwt3ltd0jgpueg7gh45w9e8",
            "ltc1qn2vt4ccwlp97zht48g6jgus8gzdmpygnq04d8y",
            "ltc1q40kd9jl65ztm7rprk2hppzrnlx05gxaejwhh92",
            "ltc1q5ftdyvpr446khsnkm28v9a7yn65ye6gzuvuwq8",
            "ltc1q6jc9q2uanpkrstmqzvmmdp7z70vzk9vzf573t9",
            "ltc1q6823j73jk7sy884wkjxceca45x3a3q6wp4xgfa",
            "ltc1qjwudfpyxtrpm6muxwzdp5up8cm79njcfqthpjp",
            "ltc1qx53wh54fm5zlj2h53a2nmrsyjz7nzrrgz9qvh4",
            "ltc1qshv47sgew9nc8jgfxy9tlk4cwekse6wjuxes3x",
            "ltc1q88yr3hay0f7kcaxxltcl5r4pq4xk5k00frmjmx",
            "ltc1qz930wkj5qw39h6yyn980kt3m0r2ewn8zq7xx3x",
            "ltc1qv06z2tzs64xec8sy6ayhpzf2gf7xnufe9kux04",
            "ltc1qf62z43e6dx7s0f9gwvn79w9hsq8pt9dcygnpq5",
            "ltc1q52z99f798m6e8lyv2r8jeavcnypkwzveupy6ae",
            "ltc1q5f6h2zkmhhjjmvcprjtq4njn6ekeur6kh7rcm9",
            "ltc1qgcayu9wmpwkkm9j2fn06uxmwgupe7l309rxny9",
            "ltc1qqke3d95cqafz76vx7fuk8yztqj9he323u4heur",
            "ltc1qey6tryuvg3ct5pv05chegcu8fmz92c7khx504t",
            "ltc1ql5awlqhm4pn4tswplxwk50mvtypsp93jsr0nwm",
            "ltc1qcvajh6g3y79yal4r05vdhcv8p9rvvy2266w42c",
            "ltc1q43k6m38wr9rhka88h5397hjrf6knej532cv8w2",
            "ltc1q8d67n9vsxdj26vqyyexwnh5wl5y63cn7wg7af6",
            "ltc1q6mgh6579xjpqxts73mqs8vws6sjlxa34kenzt3",
            "ltc1qp6hqn9d37rgudvcsjprlequr0k7cvrjmcph3r5",
            "ltc1qwadtl4mr0wlh7szvvzdp7gdh6qxfnerh5hkz8f",
            "ltc1qmk0h4eueez4hgfl4v0lcjvnqhaz349raau5yv3",
            "ltc1qmuf8aawtnvqlv3gxnnfgwy2mh49fwhwzv4zmz3",
            "ltc1qrmsp7r9hy8wrx76k2pz7u76a6quluzt4y7m04l",
            "ltc1qpr94e87vwuhvy2ehwp0c4krnkfqcwed7c29qk4",
            "ltc1qeq2dykxzjtvaqp4zpfsdhqxdwtyppeszt48rpp",
            "ltc1q3t69et2j0u3vs35ge3pxhdjmt73fgclj37lcsf",
            "ltc1q693fx2aetddzjrxhfa47a22fkdss7dyr9g93rw",
            "ltc1qfl0yq6ktflgv6rwhdrw5unkud8h0qw69tjecuj",
            "ltc1q8ld86mwahhqkv62mlnd2tdluk5vr82qpk6nuzl",
            "ltc1qgfwmswz7smt92rsxrrukqfukvnajfjlv89w7sv",
            "ltc1q8vfscmuemp7sc6j7220klxvsv75hqjlh4r0u3x",
            "ltc1q82x9al7nf696aakk9n74g7heq2rqgm76l2zc8h",
            "ltc1qvsgpqg369rxv0epdhua8cq2udzz8uffrep9tcg",
            "ltc1q20uuqpjhk83zr95ns6r6nl8jkpt9h3fplhrls5",
            "ltc1q349wgqstda3mufdw0x9ftuj3kvf304wlqzvcf6",
            "ltc1qkvc0767s57lr29m2mku3s0qyua7qnd0kyzkwxe",
            "ltc1qvjruvvptgd5lwf7hkxqn7ehk5n64kpjdxkzvnv",
            "ltc1q777xpa6pzcdgns727n66zzl3lkcmrn6rzdm5yg",
            "ltc1q8p8jatkaeg9dwh6r6aw9wncrc0yqgxsmle2mvp",
            "ltc1qpgmqa9sp7qq296vcawrr4en4j9gxwg2zx0vtum",
            "ltc1qc39wg395rmldsanxxpyf3mcxwxa0f68yqzap7y",
            "ltc1q3fg0wuzjnfx5cd8z989pqkhdu7qm2capxtan2a",
            "ltc1qe4r80p5scngtdp7cmc3rvkamww0vsyy8nmj5qf",
            "ltc1qrwa0yrd2cq03d73glvxsa4c4675kt7x6dq3704",
            "ltc1qh8fpqthwtxk9pjfz28z5ylu5kr3l8ztdl35a7k",
            "ltc1q9grnwu26unynvrvz43dgnx7wzulpq5auudq3vs",
            "ltc1qqdnasy3tmtfzanznqswc2m3j5xyu4kccxddsgc",
            "ltc1qw2phpjfzgpm5v2gd3zsqnnmjzg3cp9e2q8fqzg",
            "ltc1qncdmpkrnqv7ma3kjrj5admz9v9trgkxu864ayy",
            "ltc1qzz82yses658c5g9gx44sh7yy5phep2u0ezchcj",
            "ltc1qrlqv5v278f0x74r2n50sej8k8vd3rcr0jfv8z2",
            "ltc1qjv3ucag4nyf9mvkm3xnnxk46ecp9348zxa98f8"
        )

        private val expectedHardenedBech32Addresses = arrayOf(
            "ltc1qghs243e8vpc0jwaqmdly2fla07am2zqrd3zz9m",
            "ltc1qcvm586sylersm6hkrkjucdtn3ncr80087tfj7v",
            "ltc1qgtfe2pjy5gvgn7ng7fcmaa26rw69ddndgztur0",
            "ltc1qcsdp82t76t3p8u9573ssam4trx0td5zrafqdsn",
            "ltc1qzp6ada4n6l7400da5t8kptjz2t04kezaj2j0sr",
            "ltc1q85c3qj7xrkepv6cwuyfwkea8nz97xjptfrzhhv",
            "ltc1q89vtxr9kpy5vxwh9n4np9x86tqlrg6xpye76ke",
            "ltc1q53mjg0zyspl3fceqycefhs2zzd52hd77jvmn7w",
            "ltc1qhp8vqst6r82840w80ef7gmnl2xdqhjwlwu6eg7",
            "ltc1qctxkmc7p2an5yl3ze9wdp3hf8cmye2kzwq98e0",
            "ltc1q2e9d75j05yx6xnssu0hzsgwyn9dscdjettqumj",
            "ltc1q4cnpxmj78uudyzjxqu9fd65hj4g62rckwzqycg",
            "ltc1qmjsn9qms87f7cz9x46h0u98cyjhn3zga3qx2yu",
            "ltc1q3mh330jpz7nm48yapswy22ep9fglx3dt566l3d",
            "ltc1qs05lnzmcr969al3jglvluk6hszmk88dwsu7vxj",
            "ltc1qpmyq0mgvklwaf5j2fhrw8asrq6a43yf69afjuz",
            "ltc1qgf2qkyyfd2vjm3437t0jvdamhdekx4wwxgv0qh",
            "ltc1q7psjf93urkuv92f2pct6872ddw8hlxy0p4g9st",
            "ltc1qcp8zplyj68akhvhtpp2dk5v7v50dhvma6wzxy5",
            "ltc1qxhm7e84h2d56lq6zel8u8d3wd3f5cg6txctvpq",
            "ltc1qglfhsnq5luw8vrd5khclxmaxmt85hzjyukwupx",
            "ltc1qv3zvf3q9uywruelcf6473ky39vs003m5cp028a",
            "ltc1qa9sd4ldatprmttkfajrs9z0yaaxwz4ldnzptwr",
            "ltc1qmjw0a67puy5sazr4qvv273f9y8jtj39zt3m5mm",
            "ltc1q3z24kpqj4gww7gu9lwxy53gv9hwpma57al6vjw",
            "ltc1qhfjupfx8fs0sgkj28yenacz4hm67rxh3fgjy8c",
            "ltc1q4j2cue463kltyk7du38qzht74cgdkqxe87kzfe",
            "ltc1qujvvpm6vvdh2zvne0yppqcchls0ah2xfr7xtqu",
            "ltc1qu5fg7kffk3uwuawzvzzwtf4kvad3way70t74qp",
            "ltc1qxetacg5mzlx95gkzkt97j5pncvuvaq8krqfm70",
            "ltc1qugkdsrd8phksy3z9yjcwrn96p32y36rugcazj0",
            "ltc1qgnptt6par8ekjjf4fwhk6sej3h9umeglqetjcj",
            "ltc1qjhp3mmrnxnd4qd6elem025hy90rpw8f30pwpyk",
            "ltc1qd6xpf5lwkfxqjyfjnh9wsdfh8epmdaml3qqncf",
            "ltc1q9xwvswmr3rgessundw79z37vqxnamcddjzfz5q",
            "ltc1qrqsnyzy6cjalhdv6sruqrfld4sdmfpen8wpzcp",
            "ltc1ql840pscfhq6fm0w7ka8k3q32ky46dc5yk9720j",
            "ltc1qu56dqvka77d68m4n8jhelcwgvmuads0eluc65f",
            "ltc1qltxlhaqlsnxmxry2atd0kq2726z33f9xfve3f9",
            "ltc1qa03zwr8dz70apvccpetwg65qaf8e6qkr2tw984",
            "ltc1qje4s08d37ne396ek3m86pu7l86znjwex9eh79m",
            "ltc1q2jg3rp272kgngh0pamq4e9zspn48detazfyp5k",
            "ltc1qmgvwqqxh07p28fpvnpd4k6tc36g86pawn5kdfg",
            "ltc1qmaks230jjvhk3ly9z2vs9fcyk9qnea2244htqk",
            "ltc1qrzyfy93xvgmewa5zddfemvm8zzweqmf7h3f8df",
            "ltc1q8qqyh5cs8dyc3nx0dz7v62tkuguefyte4vc3n5",
            "ltc1qufyycpnt2xwahprctyrpa5jg8j2n8dxzrkketd",
            "ltc1qrnzxcygtw43lx0ev6q9jnzvznlh0mjy57etsz9",
            "ltc1qvku22mputu5ga9k5kccf5g69zhe4yf0u4vg5gx",
            "ltc1qars0sn3tvzetthgzz2qyeyn6c4cedjpxtxy5rc",
            "ltc1qmgy0eesj7eugrfn9n2p8kmvylnp8drurcz5d9j",
            "ltc1qeu8aptc0r7j2ealzgce07tfnyd5wpvex9vy2qm",
            "ltc1q3j06k8l8dgmkdmsgntyg5vl4lsjjsempuqk4gr",
            "ltc1qq2yg2e978ja0prafkqdl9muqxdzqk9f7q4qe0u",
            "ltc1q967umpqkqr5feszd27xeykqfx778dxdc49t3sv",
            "ltc1q0c6e8tr5hyynnannsl4f3r8zkrsxjun9shve67",
            "ltc1qhcejhcrv5ldvz0f5pu0s5mdxpr38vlfnfp5svn",
            "ltc1qx5y9gase0t2w059sdc7ujf2vc52lnf9mk85u3j",
            "ltc1qgmefntzctvt89rde4pw0zfguxpkcuxvw39d5xx",
            "ltc1qpgvyh306uryyktrxxqukd5k5ryd87k4fk6q27s",
            "ltc1q5vhdc6k9cx7kmfw58s32s4rdnm6fz0dxchhq8k",
            "ltc1q5dzn78shfxk9luau6w50nc7e3ptv6cx4tkn3lj",
            "ltc1qcj85twehy24gdyyeswvz6sjv52ykffxcrn8dgn",
            "ltc1q5z2qesxg5pwr0y5j4n58cvefvk2srlwznq8rga",
            "ltc1q8v7d50d9vnfnfqz9v44c98c7d3a6f7hzs7ucam",
            "ltc1qhzge0v4d5rzsncflvs4hjguuw2ft757xpjqtjt",
            "ltc1qcsn6465gq6932vgzlgl6mgqtr4nhdu3fcyet44",
            "ltc1qshnr95k0gkhtcplhmdrc55lw27lcluz4uur73w",
            "ltc1qz8gn5y06h7dr3930qnfgu0hcnj2s808fc8rrqr",
            "ltc1qrgn4yr597cl9sha66ugznv0v8qm6ryvkk2y7x6",
            "ltc1qjy894u2u9juw7fhfq2uv4vl34nlpyc3r7zp995",
            "ltc1qxrnthm4h6ttjzn90e2s2048wyecgs9ukpdf5tw",
            "ltc1qdj06x3kqc8rag76mgrr9qjrqer03wm6pjeeuew",
            "ltc1q62pexjnst9y72e9eeachcqhxj4tqac6mcy8694",
            "ltc1qxrszjs5wt2pkxamf0kc40s0ydhqyhk7u24uf2v",
            "ltc1qp98hh7lgult9amlra3c4ujkwkkquhra7hd3y4d",
            "ltc1qr0kasf6l93tl6pnvsazjvmk6yaqe3nkkem3r49",
            "ltc1qh8zfmudf7kftx0f6yrrggp69ehcz8k4s9mgu6h",
            "ltc1qngc6h0hurj5ywkhcj7lkz5t552uf2shlwvcdmj",
            "ltc1qnugsnkl7wvn5fjtgwqhupccuzg7uj6tkhpdqk9",
            "ltc1qweetmlp9ec9gm47pplulej86sfyyf8mgejv77d",
            "ltc1qpr0axrnjttx5gzmkmudz7s0xvj0gf54wauek9y",
            "ltc1qppzrruvnex6w995pnkyurlf4a56r6uzmst8raj",
            "ltc1qh3lyyajncvq09xgzhkpjqxx9rlfxrs8xwvqqyk",
            "ltc1q4txe0uhpz68w4raahttxyn8657jlq252d4v8d5",
            "ltc1qzuqzrqdka8aggxx950vwu75t9fxh5elk7l2th3",
            "ltc1qfp0kvr3dhqn3cjet66r4mkcffyttnc2ltcf287",
            "ltc1qdp8rudzlp99czquyw9t5q2823yemz8eayw9lsq",
            "ltc1q6xf3tq6za9kj24udj5svx2x82wutm2qksxx8j9",
            "ltc1qvleqdt0s07pfmk488z8scasma48tsem4wdx6a0",
            "ltc1q9d8gc0mx30646ql6e3r24n55yf4nu7ey47m80s",
            "ltc1qt6nnt4dzdhqcdl2jmg2d6cv986y9lgkldatcg3",
            "ltc1qhjttdtnj4zy0k3jm6q4ln25fll362feauhk2n2",
            "ltc1qv0z006e3etdp5ylnxh5nszj2p2u0mttz9lzl4h",
            "ltc1q8dnp36jdyr5de4jexqk4kr4jnet02tf69ahpek",
            "ltc1qwn5tfzgmtwccwvqjguk7aqj3njgnqyzln9e95u",
            "ltc1q7n39rtypn0v2l64ewysm3vrs52rcj7hatq5v69",
            "ltc1qddsyj987ll76gckmwkdsefzv92a52hl24re839",
            "ltc1q6rppmkxlu5wf666hg8gc0twmltd43duph2r9sz",
            "ltc1qcyuz3hlft7k2njdjw8fdhdj6q7dhjlhssesa0p",
            "ltc1qa34fwvdruncjqaqua5ye5jtvx2hw9pk87n0p6s"
        )
    }

}