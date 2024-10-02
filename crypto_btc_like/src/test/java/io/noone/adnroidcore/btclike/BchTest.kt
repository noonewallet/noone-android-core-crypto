package io.noone.adnroidcore.btclike

import io.noone.androidcore.hd.ChildNumber
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.utils.hex
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.addresses.BitcoinCashAddress
import io.noone.androidcore.btclike.networks.BchParams
import io.noone.androidcore.btclike.transaction.*
import io.noone.androidcore.btclike.transaction.bch.BchScriptSigProducer
import io.noone.androidcore.btclike.transaction.bch.BchSigPreimageProducer
import org.junit.Assert
import org.junit.Test

class BchTest {

    private val seed = "c4eb38a2e9a353c180e8e74c474e9192633f81afdbaa49d0c6207def597dafc1".hex

    @Test
    fun testBchTransactionBuilder() {
        val expectedHash = "740a30123c51b0b45126b69d52bd730a6235673c15003970479a7cabb1a6f0c1"
        val expectedRaw = "01000000018b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786000000006b483045022100cd1a3cb33b1d74a7a45c28d62ba3bda7988292259c096f0da38261121f109ab502203c7664eb076407e8b384e4f1329e5d2884d0cf4ebea413a726ce525b051e05f94121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b1ffffffff0228230000000000001976a9149260e1a82c0f394549eb2f4a79f9d15ce586d4ab88ac9c5d0100000000001976a914757af685b9736dffa7327954e60f38148f0bedd588ac00000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BchParams)

        val receiverAddress = "1ELyiT6djUqaHqQKpVA48Yw3EoMnS7QD8w"
        val changeAddress = "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            BchSigPreimageProducer(),
            BchScriptSigProducer(),
            ScriptPubKeyProducer(),
            BchParams,
        )
            .from(
                UnspentOutput(
                    "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
                    0,
                    "76a9143f6d330ab1274be7ee7ec75a387e819874c6f9c688ac",
                    100000L,
                    senderKey,
                ),
                Sequence.MAX
            )
            .to(receiverAddress, 9000)
            .withFee(1500L)
            .changeTo(changeAddress)

        val tx = txBuilder.build()

        tx.splitTransaction.print()
        tx.rawTransaction.print()
        Assert.assertEquals(expectedRaw, tx.rawTransaction)
        Assert.assertEquals(226, tx.txSize)
        Assert.assertEquals(expectedHash, tx.hash)
    }


    @Test
    fun testBchAddressesGeneration() {
        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val addresses = (0..100)
            .map { index ->
                BitcoinCashAddress.fromKey(BchParams, key.deriveChildKey(index)).base58
            }
            .toTypedArray()
        Assert.assertArrayEquals(expectedAddresses, addresses)

        val hardenedAddresses = (0..100)
            .map { index ->
                BitcoinCashAddress.fromKey(BchParams, key.deriveChildKey(index, true)).base58
            }
            .toTypedArray()
        Assert.assertArrayEquals(expectedHardenedAddresses, hardenedAddresses)

        val bech32Addresses = (0..100)
            .map { index ->
                BitcoinCashAddress.fromKey(BchParams, key.deriveChildKey(index)).bech32
            }
            .toTypedArray()


        Assert.assertArrayEquals(expectedBech32Addresses, bech32Addresses)

        val hardenedBech32Addresses = (0..100)
            .map { index ->
                BitcoinCashAddress.fromKey(BchParams, key.deriveChildKey(index, true)).bech32
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedBech32Addresses, hardenedBech32Addresses)

    }

    @Test
    fun testAddressEncoding() {
        val expectedAddress = "qzpzd8n2hvzuelqpuhsxmmnfq47hlj8ka5unj9t8a5"
        val expectedBase58Address = "1CsB7DnDMorterzoE4TrqVsDW8PLaYVgjj"

        val seed = "3929e068896221bd59b37cc5cac18982862e155381cba62d03513712e9286396a27da9d965d6123eb16b4eef82c4ddcdfd748898b71841bb7342a3c255ca0f58".hex
        val bchPath = "44'/145'/0'/0/0"

        val addressKey = DeterministicSeed(seed).getKeyFromSeed(bchPath)

        val bitcoinCashAddress = BitcoinCashAddress.fromKey(BchParams, addressKey)

        Assert.assertEquals(expectedAddress, bitcoinCashAddress.bech32)
        Assert.assertEquals(expectedBase58Address, bitcoinCashAddress.base58)
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
            "qzfxpcdg9s8nj32favh5570e69wwtpk54v0tqn5ct4",
            "qp6h4a59h9ekmla8xfu4fes08q2g7zld65yyuacqsv",
            "qr9t3leugnyetk9nn8rrfckl8rt0cgd3lue9h8ezrc",
            "qq45fmg6sl0pmch9t7fc769hkl49d2ed0c49xctlgh",
            "qzqsqgezrnygkerhuz626mzrqpp3gnghcsj8f3ckmj",
            "qqlfdz6e5gg0xc2hrqu83sl72hj2xwlufcz5evr3al",
            "qruhm9as6rrwxwfaq2ryypdmm767cdtm2ujuv8uud3",
            "qpwtexmwchq23wrlmlhlk75sslnt9kr7lqc7j0qg94",
            "qpuck6k537reygn8qdnp4rfk7m5d3qe7056pmrceam",
            "qqcexeh7sa9ezv20dky2hsjvuqjnzl4jvvd9dz6x43",
            "qzv52fsususszwxp79s5hkj8lurxxxwh8c8vuguw2c",
            "qq35dsn5vgu8jda3z5ux0nsg2h9jtmlagu90shtdky",
            "qpest7mdy0vlv22ppum9wtkcwzuxpyn9v5g05l95m9",
            "qp52mv9z3e8jepayurlgvjqs4g20lqjyv57f82amec",
            "qpnr542qtq97thfs6l695fz3xytutffmhcwtr5zplm",
            "qzyekn8svm2z3c2nmcj7up3wq7sv8qdrp5glr4n3lg",
            "qrejm2xlsqdg2z702tnqhlv4gyy60q8xyq6g4u8r36",
            "qprv2hmtdsg6vtq6g57nmlraka3z6440mgpuv7qjmx",
            "qzpfsyd0kamvj0nzjyza75wv9ggmhxq5tv280zjte8",
            "qqk8eng7au5rchz9cqalu50q4jx9rzn0rcqv6qktdd",
            "qzvd2z3pslsa7ssnqp5xc9ssfxntpc5tdqdjgdm505",
            "qq279ctnxxj4kgh3nemq7sjfvhy5w7ml5urgfnmvag",
            "qrszmzmd7qvkwth0f8zdjpntsmsy8f49du5rjjfn2g",
            "qq3nfm5328tc732a8tv05r77rft3ku5fc5xyyfnnz0",
            "qp7yladmucxslrmj2tnqyfyc7fy7wwsx4y5zvwnj35",
            "qpacy0yz7wrvlr8dfln60rdx9m7t8h508gf30dnhe5",
            "qq7w0dgw344qa4lrztq5l72vh9h78kqe4gax7qcj9t",
            "qpv76tl7a3hunkdjsvt7aew8wukeh0ft8ss3429d04",
            "qrt9lqg6tga6qypsur0kgz9f3flfme2fju78ewce82",
            "qqv5qsc0c7rt5n4m0gpq20ehg4tqk5lpa57x6jr4uh",
            "qzx0j8j2qu2wu3cmqj9d4pvuj7yfdez9csdxaz0qtk",
            "qq0ge5kqwl5alwc5wc07fgmwlaj0tgzc2gf6kmtp2p",
            "qzlrr652jyxmz97ahnjv827uwa94ap299u68syrsax",
            "qryer3y7eqkprv92xf3cnak4hgqqknaksgus0tcpnt",
            "qzyht2k45eczclesexs7n63feyp53fe0tczeh3q8qk",
            "qzc9qexf882tyglamr7gx6mfutygk70yzug40mdv3p",
            "qr0h5ara8mqeruufpq7k25uu4wdxad4nru86n8d6r3",
            "qqtq6vgx0g86hkvvfnj70u8yluw727pw0vcun3pvfr",
            "qrgldkpdd30cwvfmex43csjsdaadx9xc0yuqz34zft",
            "qrpnnt3yn3qscdgryp9y67zraef249jf2qgh7keaul",
            "qrn8zhdus9zp93f2upefg4pxh3d36u270s26wfx3un",
            "qzpph9y5z2ymv37xnupveh29wet3lwu44ssule9gca",
            "qpusmeg3tu83hap86zxz67tg5g4m42e6vyh7js6na9",
            "qz7tx46djwnsf8qzn8rt86gwv22mvhl23s9z5yvclk",
            "qqgh3dzq0rdfda4sldew8ad47fq8n9rezu9dpva9sm",
            "qzdf3whrpmuyhc2aw5ar2frjqaqfhvy3zv9qqvcuet",
            "qz47e5ktl2sf00cvywe2uyygw0ue73qmhyrljq0c2v",
            "qz39d53sywkh267zwmdgashhcj02sn8fqgrcg0mxsl",
            "qr2tq5ptnkvxcwp0vqfn0d58cteas2c4sg2glnqxzr",
            "qrga2xt6x2m6qsu7466gmr8rkks68kyrfcxvvy49m4",
            "qzfm34yysevv80t0secf5xnsylr0ckwtpyn95duqha",
            "qq6j967j48wst7f27j8420vwqjgt6vgvdqjfq7p9zc",
            "qzzajh6pr9ck0q7fpycs4076hpmx6r8f6gwd0rlmwu",
            "qquuswxl53a86mr5cma0r7sw5yz566jeauljhnl86w",
            "qqgk9a662sp6yklgsjv5a7ew8dudt96vug7c8kng8u",
            "qp3lgffv2r25m8q7qnt5juyf9fp8c6038ye7srw0js",
            "qp8fg2k88f5m6pay4pej0c4ck7qqu9v4hqnptc2e0s",
            "qz3gg548c5l0tylu33gv7t84nzvsxecfnykgcst06f",
            "qz382ag2mw772tdnqywfvzkw20txm8s02cnufzwp86",
            "qprr5ns4mv966mvkffxdltsmders88m79u4578m39h",
            "qqzmx95knqr4ytmfsme8jcusfvzgklx92y9z5pu7e5",
            "qrynfvvn33z8pws937nzl9rrsa8vg4tr6cwnk8kw90",
            "qr7n4muzlw5xw4wpc8ue663ld3vsxqykxgl3e9lykz",
            "qrpnk2lfzync5nh75d733klpsuy5d3s3fg7avqyecq",
            "qzkxmtwyacv5w7m5u77jyh67gd8260x2jyd4fxus49",
            "qqaht6v4jqekftfsqsnye6w73m7sn28z0cuyzqnga3",
            "qrtdzl2nc56gyqewr68vzqa36r2ztumkx528pauukk",
            "qq82uzv4k8cdr34nzzgy0lyrsd7mmpswtv8uc324fk",
            "qpm4407hvdam7l6qf3sf58epklgqex0ywutssx9cvw",
            "qrwe77h8n8y2kap8743llzfjvzl52x5505ghgqlzrs",
            "qr03ylh4ewdsraj9q6wd9pc3tw75496acgsuxsqnle",
            "qq0wq8cvkusacvmm2egytmnmthgrnlsfw5g806jwe2",
            "qqyvkhyle3mjas3txac9lzkcwweyrpm9hc2vmuwuvw",
            "qrypf5jcc2fdn5qx5g9xpkuqe4evsy8xqgapfwyngl",
            "qz90gh9d2flj9jzx3rxyy6aktd0699rr7gpx0463yw",
            "qrgk9yeth9d452gv6a8khm4ffxekzre5svcl3sdkpl",
            "qp8ausr2ed8apngd6a5d6njwm357aupmg5fhxmyhhf",
            "qqla5ltdmk7uzenft07d4fdhlj63svagqywxqkw3nc",
            "qpp9mwpct6rdv4gwqcv0jcp8jej0kfxtas4f5yzxe7",
            "qqa3xrr0n8v86rr2teff7muejpn6juzt7utw0vlaes",
            "qqagchhl6d8ghthk6ck064r6lypgvpr0mg9kd6f7zy",
            "qpjpqypz8g5ve3ly9kln5lqpt35ggl39yv8fykwxac",
            "qpflnsqx27c7ygvkjwrg020u72c9vk79yy7dsts6hq",
            "qzx54eqzpdhk80394euc490j2xe3x974mul0kvucjp",
            "qzenplmt6znmudghdtwmjxpuqnnhczd47ckthph6w9",
            "qpjg033s9dpknae867ccz0mx76j02kcxf5fc3k6y7x",
            "qrmmcc8hgytp4zwret60tggt787mrvw0gvj8t9e9rm",
            "qquy7t4wmh9q446lg0t4c460q0puspq6rv75nj8j09",
            "qq9rvr5kq8cqpghfnr4cvwhxwkg4qeepggyk33hp27",
            "qrzy4ezyks00akrkvccy3x80qecm4a8gusz0y55xf4",
            "qz99pams22dy6np5ug5u5yz6ahncrdtr5ynudmm0d6",
            "qrx5vauxjrzdpd58mr0zydjmhdeeajqssu0efauwq4",
            "qqdm4usd4tqp79h69ras6rkhzht6je0cmgvwjqsp89",
            "qzuayypwaev6c5xfyfgu2snljjcw8uufd5sz05crjx",
            "qq4qwdm3ttjvjdsds2k94zvmectnuyznhs7mx2jvh9",
            "qqpk0kqj90ddytkv2vzpmptwx2scnjkmrqq7rx3cwz",
            "qpegxuxfyfq8w33fpky2qzw0wgfz8qyh9gjxewrfeh",
            "qz0phvxcwvpnm0kx6gw2n4hvg4s4vdzcmsltcge294",
            "qqggagjrxr2slz3q4q6kkzlcsjsxly9t3u8l229m8j",
            "qq0upj33tca9um65d2w37rxg7ca3ky0qduh4pk72ha",
            "qzfj8nr4zkv3yhdjmwy6wv66ht8qykx5ugatmcmq0k"
        )

        private val expectedHardenedBech32Addresses = arrayOf(
            "qpz7p2k8yas8p7fm5rdhu3f8l4lmhdggqvu0fsktve",
            "qrpnwsl2qnlywr027cw6tnp4wwx0qvaauug529prp7",
            "qppd89gxgj3p3z06dre8r0h4tgdmg44kd5rn4w7wv8",
            "qrzp5yaf0mfwyylskn6xzrhw4vveadksgvfhv8wpju",
            "qqg8t4hkk0tl64aahk3v7c9wgffd7kmyt50wqq6ce7",
            "qq7nzyztccwmy9ntpms396m857vghc6g9v345c27w6",
            "qqu43vcvkcyj3se6ukwkvy5clfvrudrgcy9tyndxee",
            "qzj8wfpugjq8798ryqnr9x7pggfk32ahmcakj2v0f6",
            "qzuyaszp0gvag74acal98erw0age5z7fmucfzueprq",
            "qrpv6m0rc9tkwsn7yty4e5xxaylrvn92cgrq780fju",
            "qpty4h6jf7ssmg6wzr37u2ppcjv4krpkty7wf8ddvt",
            "qzhzvymwtcln35s2gcrs49h2j724rfg0zcdyppg8y4",
            "qrw2zv5rwqle8mqg56h2als5lqj27wyfr52523nshm",
            "qz8w7x97gyt60w5un5xpc3ftyy49ru694vzm95vv2p",
            "qzp7n7vt0qvhghh7xfranljm27qtwcua4ch7pwjtf5",
            "qq8vspldpjmam4xjffxudclkqvrtkky38g8v4hrnll",
            "qpp9gzcs394fjtwxk8ed7f3hhwahxc64eccjefgrau",
            "qrcxzfyk8swm3s4f9g8p0glef44c7luc3uq2ack0mk",
            "qrqyug8ujtglk6ajavy9fk63nej3akan0527frr5ls",
            "qq6l0my7kafknturgt8ulsak9ek9xnprfvtm4qagx2",
            "qprax7zvznl3casdkj6lrum05mdv7ju2gs8nnlvpxm",
            "qpjyf3xyqhs3c0n8lp82h6xcjy4jpa78wstrm5lcu4",
            "qr5kpkhah4vy0ddwe8kgwq5funh5ec2ha555ck2h4x",
            "qrwfelhtc8sjjr5gw5p33t69y5s7fw2y5gw8zaqc9m",
            "qzyf2kcyz24pemershaccjj9pskac80knclmm95vk6",
            "qzaxts9ycaxp7pz6fgunx0hq2kl0tcv67yhhklky24",
            "qzkftrnxh2xmavjmehjyuq2a06hppkcqmyfkwca6y9",
            "qrjf3s80f33kagfj09usyyrrzl7plkageyaqwlepu5",
            "qrj39r6e9x683mn4cfsgfedxken4k9m5nc258g757e",
            "qqm90hpznvtuck3zc2evh62sx0pn3n5q7cx6kr56l3",
            "qr3zekqd5ux76qjyg5jtpcwvhgx9gj8g0s6tdsk5x2",
            "qpzv9d0g85vlx62fx4967m2rx2xuhn09ru86fqk5x3",
            "qz2ux80vwv6dk5pht8l8da2jus4uv9caxy7254qmg9",
            "qphgc9xna6eyczg3x2wu46p4xuly8dhh0u49tvz9rx",
            "qq5eejpmvwydrxzrjd4mc528esq60h0p45gjysntpq",
            "qqvzzvsgntzth7a4n2q0sqd8akkphdy8xvggzkx5r0",
            "qru74uxrpxurf8dam6m576yz92cjhfhzssyyquwk4q",
            "qrjnf5pjmhmehglwkv72l8lpepn0n4kplyr52j6l7p",
            "qravm7l5r7zvmvcv3t4d47cptetg2x9y5ccxvuw5gq",
            "qr47yfcva5tel59nrq89der2sr4yl8gzcvs9jdgr9d",
            "qztxkpuak860xyhtx68vlg8nmulg2wfmyche9adkey",
            "qp2fzyv9te2ezdzau8hvzhy52qxw5ah905pc7n767y",
            "qrdp3cqq6alc9gay9jv9kkmf0z8fqlg84catz3wsh5",
            "qr0k6p2972fj768us5ffjq48qjc5z084fg3n0mgjq7",
            "qqvg3yskye3r09mksf4488dnvugfmyrd8ces6pel2l",
            "qquqqj7nzqa5nzxvea5tenffwm3rn9y30y83xsqs7r",
            "qr3ysnqxddgemkuy0pvsv8kjfq7f2va5cg8g7zh7uj",
            "qqwvgmq3pd6k8uel9ngqk2vfs207alwgjsths3c6z3",
            "qpjm3ftv830j3r5k6jmrpx3rg52lx539lsn0lu9l27",
            "qr5wp7zw9dst9dwaqgfgqnyj0tzhr9kgycszyvamd9",
            "qrdq3l8xztm83qdxvkdgy7mdsn7vya50sv45e8elaa",
            "qr8sl590pu06ft8hufrr9ledxv3k3c9nyckunlqta6",
            "qzxfl2clua4rwehwpzdv3z3n7h7z22r8vypm589ndh",
            "qqpg3ptyhc7t4uy04xcphuh0sqe5gzc48csj8njtgk",
            "qqhtmnvyzcqw38xqf4tcmyjcpymmca5ehqs7h6dl2m",
            "qplrtyavwjusjw0kwwr74xyvu2cwq6tjv5k0qwaxj0",
            "qzlrx2lqdjna4sfaxs837znd5cywyanaxvlm5zaan4",
            "qq6ss4rkr9adfe7skphrmjf9fnz3t7dyhv2663tnmp",
            "qpr09xdvtpd3vu5dhx59euf9rscxmrse3cqcuue4c9",
            "qq9psj79ltsvsjevvccrjekj6sv35l664yjt02ykzv",
            "qz3jahr2chqm6md96s7z92z5dk00fyfa5c397yxp62",
            "qz3520c7zay6chlnhnf6370rmxy9dntq655v8yusm3",
            "qrzg73dmxu324p5snxpest2zfj3gje9ymqjudpdfh7",
            "qzsfgrxqezs9cdujj2kwslpn99je2q0acg78q5vwr4",
            "qqanek3a54jdxdyqg4jkhq5lrek8hf86ugps3de9gg",
            "qzufr9aj4ksv2z0p8ajzk7frn3ef906ncc90jg4dlm",
            "qrzz02h23qrgk9f3qtarltdqpvwkwahj9yxpuknqz3",
            "qzz7vvkjeaz6a0q87ld50zjnaetmlrls25v3a38hwa",
            "qqgazws3l2le5wyk9uzd9r37lzwf2qauay3xhnwyqk",
            "qqdzw5swshmrukzlhtt3q2d3asur0gv3jcmj95etkg",
            "qzgsukh3tskt3mexaypt3j4n7xk0uynzyv2erz02kz",
            "qqcwdwlwklfdwg2v4l92pf75acn8pzqhjc2u5fkgwr",
            "qpkflg6xcrqu04rmtdqvv5zgvryd79m0gych0xjdh8",
            "qrfg8y62wpv5netyh88hzlqzu624vrhrtvehmza26w",
            "qqcwq22z3edgxcmhd97mz47pu3kuqj7mms2r64yxnj",
            "qqy577lmarnavhh0u0k8zhj2e66crju0hcjexfssuv",
            "qqd7mkp8tuk90lgxdjr52fnwmgn5rxxw6c0pcpy2c3",
            "qzuuf803486e9vea8gsvdpq8ghxlqg76kq70mzh9gl",
            "qzdrr2a7lsw2s366lztm7c23wj3t392zlu95kuzvw6",
            "qz03zzwmleejw3xfdpczls8rrsfrmjtfwcux3m0ze0",
            "qpm8900uyh8q4rwhcy8lnlxgl2pys3yldqg4wkgw27",
            "qqydl5cwwfdv63qtwm035t6puejfapxj4cpydp8c2c",
            "qqyygv03j0ymfc5ksxwcns0axhkng0tstvhdexn64x",
            "qz78usnk20pspu5eq27cxgqcc50aycwquclstenprs",
            "qz4vm9ljuytga650hkadvcjvl2n6tup23gvmzg26fl",
            "qqtsqgvpkm5l4pqcck3a3mn63v4y67n87cqalj3awj",
            "qpy97esw9kuzw8zt90tgwhwmp9y3dw0ptufzj3e8za",
            "qp5yu035tuy5hqgrs3c4wspga2yn8vgl85kmy5u0ny",
            "qrgex9vrgt5k6f2h3k2jpsegcafm30dgzc3ktkmg96",
            "qpnlyp4d7plc98w65uug7rrkr0k5awr8w5f7dctcfd",
            "qq45arplv69l2hgrltxyd2kwjs3xk0nmysyx2998f7",
            "qp02wdw45fkurpha2tdpfhtps5lgshazmueathhk4m",
            "qz7fdd4ww25g376xt0gzh7d238l78ff885z8ssfqlm",
            "qp3ufaltx89d5xsn7v67jwq2fg9t3lddvg8vw9ljcz",
            "qqakvx82f5sw3hxktycz6kcwk209dafd8g4l7urcmw",
            "qp6w3dyfrddmrpeszfrjmm5z2xwfzvqstuahag0kc9",
            "qr6wy5dvsxda3tl2h9cjrw9swz3g0zt6l55nppwft4",
            "qp4kqjg5lmllmfrzmd6ekr9yfs4tk32laghsalwmfk",
            "qrgvy8wcmlj3e8tt2aqarpadm0adkk9hsy8z5z05pj",
            "qrqns2xla906e2wfkfca9kaktgrek7t77qmd93hmlz",
            "qrkx49e350j0zgr5rnksnxjfdse2ac5xcu7477axjd"
        )
    }
}