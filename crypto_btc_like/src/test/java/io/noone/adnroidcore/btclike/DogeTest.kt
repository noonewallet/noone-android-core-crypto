package io.noone.adnroidcore.btclike

import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.addresses.LegacyAddress
import io.noone.androidcore.btclike.networks.DogeParams
import io.noone.androidcore.btclike.transaction.*
import io.noone.androidcore.hd.ChildNumber
import io.noone.androidcore.hd.DeterministicSeed
import io.noone.androidcore.utils.hex
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DogeTest {

    private val seed = "c4eb38a2e9a353c180e8e74c474e9192633f81afdbaa49d0c6207def597dafc1".hex

    @Test
    fun dogeAddressGeneration() {
        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val addresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(DogeParams, key.deriveChildKey(index)).toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedAddresses, addresses)

        val hardenedAddresses = (0..100)
            .map { index ->
                LegacyAddress.fromKey(DogeParams, key.deriveChildKey(index, true))
                    .toBase58()
            }
            .toTypedArray()

        Assert.assertArrayEquals(expectedHardenedAddresses, hardenedAddresses)
    }

    @Test
    fun testDogeTransactionBuilder() {
        val expectedHash = "be8bd61419aac9d2e664cfac711fba4e992c87a14191de416633acecb61731a3"
        val expectedRaw =
            "010000000186670b68298032275e3c140925634ec67b1d54e409859a7968f1f668478a328b000000006a47304402202dbee3758a617d91e90bba63a96d8bc076c96d5d18b20a2a1b19ee66b73b3db702203e80b80601250ba8fdcd90f4ba7456c7f5f92e1ba1f6ce9fe1fe3e0867045c8f0121034ebb955a1cdd8685684ce5894fb3ebb971284bc181b512f5db2441e39f8721b1ffffffff02905f0100000000001976a914757af685b9736dffa7327954e60f38148f0bedd588ac34210000000000001976a9142b44ed1a87de1de2e55f938f68b7b7ea56ab2d7e88ac00000000"

        val key = DeterministicSeed.createMasterPrivateKey(seed)
        val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), DogeParams)

        val receiverAddress = "DFrGuKX2dKj9ru9GqUwY83Hcps3oUczDUV"
        val changeAddress = "D95tAajqBYDHDh1imeJXENgTH4qemHy7H4"

        val txBuilder = TransactionBuilder(
            WitnessProducer(),
            SigPreimageProducer(),
            ScriptSigProducer(),
            ScriptPubKeyProducer(),
            DogeParams,
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

    private fun Any.print() = println(toString())

    companion object {
        private val expectedAddresses = arrayOf(
            "DJV5Fi3H2tjrpqavZ59cgK6e7w65mNQsFJ",
            "DFrGuKX2dKj9ru9GqUwY83Hcps3oUczDUV",
            "DPczepryLuPBf15vxSaMrpRkWHWmEy4iRJ",
            "D95tAajqBYDHDh1imeJXENgTH4qemHy7H4",
            "DGuBsYHCYunKddwb3NU59AFWL8QtewghXV",
            "DAr2n9zMevHHG828SzSEmftgCu4Gust5cc",
            "DTtHFM5YuUuZG7fcNHV9JD8WbuLEud5ufm",
            "DDbSeZYmp4Uhc2LEd8CNkSLxVgZYmrWFfC",
            "DGDmKMVe85VZig92ErJpnJry1QAX3VHddJ",
            "D9fEEMJtgiNv6tgP1QJV88WGPbY84JaDtV",
            "DK7WmA2QCLUB296Jb8yfMoWcGcN1PVe3GS",
            "D8McxSBF3F2gLnJC1tuypCewpZ53KikaxH",
            "DFdHQxvBW67A4M31TJ6x57JSrUJX9Q1KoQ",
            "DEgatQAZhBBS3ADqn6eofRumXNHidSjYs5",
            "DETdLseChsPp4knNowZRkKc4N4hzy61bPo",
            "DHghA1YJMWii57tzH2oPAjXydzCjojNWfq",
            "DTJuSMxBuKjxygTNvsWQiF1nHTpo9kb4iU",
            "DBbJHPFbjcDmfd6qjru6qe6Y2EzxX4i1tn",
            "DH3cZ9BMSpKQaEEVMyBUJuQowsMbNtKTti",
            "D9CKn3PnnBSwEbSsVG5kwEQDqhRAokvYJA",
            "DK5CTfZvQoWd617P3UXkQx13Lkh9syd3aX",
            "D78pXEPw3cSPxbTrkBk6ha6zPJ7ScVGPLe",
            "DRaSTj4MvJ7QMJrg5yJuwDnJ4PSRBKqX97",
            "D8MFbkCu1rneX5nuimcb6H1fLXqo6stBcp",
            "DGUQ7kqH8yv72jcHsXGgEBn9o329odKoXt",
            "DGQ9fqQda4Nq53HQjJXVTpAbZopHfgqjNY",
            "DAh8f53HzU1Y6GLKqxZeySKyEXQUufrvUM",
            "DDLapQY8FEAcYEkPE4EdaSWvLJQdUp4o3A",
            "DQgbZ4mhCgPmmRkM416dCBzCHbz69Jx45L",
            "D7ScR43yQR7FoZye5GJgFpE862AMbGKdja",
            "DHzVaAAWdhPFKG59ttUncmwMhCo2uASXjH",
            "D7vdWaqg29KTAaJMr6KLrgTKqmvkaRo9TJ",
            "DNUkiweUFU4SrFVpxh7LYJeR3Y5mk9rBbT",
            "DPWtyxWYV8xZHQD7UMhYvoh9aAjzcbRF6D",
            "DHfv5D2KhXoisuVgNTVSSP624ZFdMLCT5k",
            "DMDMmsZH7WXwjDXxi25NiULw4Le1mPC17o",
            "DRWjvMvz1Qzk3H2LuYfMhJhLnRKfX3JSBQ",
            "D79hD3JfSRQ6LwKaGTpic3B37wVnecS912",
            "DQHHUQMuQEzS3Er3QCjcFrYR5RXk7u9rBs",
            "DNwMQMnNcxW3RaTNx57VB7oh9h93vF2xHh",
            "DS9ZmCSrMmaQUDi21rcaP7geqiz9xa7351",
            "DH13RWq3S1nggLRJzc144Ju4aKoPLycXPN",
            "DGBAwtzkMR2bzCapLfFwfczYnqhpMDHR4c",
            "DNLrR9yhCVvMvuKKhH4ct3anGe85KBcBiG",
            "D6jUdBrtnDUuADhtnhhkjXMucdVLZtyq6Q",
            "DKEXZfjawHQr1Esi8zwytAHnkfN3VYGN1q",
            "DLp9nqkzWJ2nWiQBUYv2kxVzA2NYh67b7m",
            "DKwU1mSgcZmyqYCHhfiUEqFFidYBTyyeB2",
            "DQXh1NzYzjo8CXzTu4UfGaS7Juu2NVa2x9",
            "DQGb3mLerEi3g5P7VLjRyPTzZ1TrzQGkgE",
            "DJcBHgU4ik1H99VaYb9CnrZYyUc27Ez6Cg",
            "D9z4Bnss8VgW3cJLMxfDwrTs467wucPQBW",
            "DHLpp3p1gXL2dL9jE1UKh1MKJHnwukaHvB",
            "DAQcusZPQfwLWsvVsTb6TdbzWxhTsunUUw",
            "D6j2ap7uUx9p2FPGrDCHzsKmDPxc8jCVvb",
            "DEFbukod6zZpqEMpbgu3oWrHQXSVBw7ZwC",
            "DCJak3y6ECkrAuEzVjaXu9P3E9P6f9hjZp",
            "DKxQXHjYzQnQtm9BnMzFPqmd8AVXKDoyYd",
            "DKx6iQPbEa5TYoyhPy6CWrBhHuaRBN77Lk",
            "DBYRgnoKXgu88qPWiJSSCQcyaX5gmR4geg",
            "D5fETVAGf8YH8qMw2xqieSq7FokrEdMWti",
            "DPUyV32vuvzKVg1uUACEkLiRS4LiiFzE3S",
            "DUE46i2vKoqtJGVtZoRoNHw8wJtupZwHuW",
            "DNwPByWBfs5VYLqGS6UMboaMnX3CsvxLCk",
            "DLrp9sMLrqN6dJxPcqPNPjGX4SuZsgrqH5",
            "DAZVfGyfeEoBPjW1h8NQgvgN3j8ZxzxZfW",
            "DQix6u9ad81Z3vboTsSbFi7n7C41kcL6He",
            "D6UiVH3VHvCR2jnisLXdYc4Zm2d6c98WgW",
            "DG2BfTrHp4oWcnqTtJfUijpXTKxLJhJBUw",
            "DRLvvXN5a3vsLBonRUfpMjxxQgYVY3FNtE",
            "DRUbPA4A8fVkTtmbESxhPFnVKLTtKJ4Qhs",
            "D7xMJEVVpgxjsUeb8owkwSJ6pUZv9996rE",
            "D5wbZctYHXmaXKxxX84dLaHxj73RQ7MiEG",
            "DPP2dLdfCaLXaxPrEMTXhv7x75TYdVVi7u",
            "DHopXJTaHiVF3YXFmtzAXi371GstbiJqSm",
            "DQEDrTHzdkw6GPm3TWJJ2Cu734jdHcKpsk",
            "DCRQAj7qHyFtM38vDZK8Zk9us4aWMSX1iP",
            "DAxirHu4ci9dEX4hgDFJ7EwSDd1F2zMRYA",
            "DBC1QfvapESDWdjugbfmAta4TkZbEPb6N7",
            "DAXTE6TFywnnvhH8yy6aV8WroMetAxvStD",
            "DAUftYDSPmdR4rMzapdGTA4XuM6EWHgvy7",
            "DEGBMX6ydNQEfUoeRfcu7BqPEHwcekxYXj",
            "DCo7p7zKvaoogHHvDATgjo4MjyEXPrrbus",
            "DJ2BXoPHYUDZNnum1p4zWUXueE4K1zU26G",
            "DMUaC233V8KfEnXCr3KyWavRitrA4c5u83",
            "DEJemCLr7gHM6YUsBL81JuuSU9gYCijeWE",
            "DTj17RjBaciGMswmPvb3g5ytvSQvdxcr82",
            "DAGqEgpub7fP573gAPEhJWswyJASYQ9dYy",
            "D65647zMAiPTYivrS7JRAUp3Vo1BR7UvPU",
            "DP2zhRRVNikHo6V7LL52JJoW7FeYgYKtLz",
            "DHkSnPM42K55rdc5LXyLDFFUtUHC5YhTPv",
            "DPrVUi75714kbib774wfyoLQ6vk29ZkJVJ",
            "D7fije6VFJuTWf7L1D7eWF1uKpmFvHdkze",
            "DN5dCCGu7Nj5AbvdkpVePWPWQ1g5Vj5XB6",
            "D8yKZjPT5e6NYu2Nos8hu7Teyb9bTbyqfy",
            "D5T6y3UNgdAF8kYz9TGD5TJWtuXN8D9qGC",
            "DFab2n5HMHkRRYj9DHtmg21w5pFZFGHwGG",
            "DKZ6UZLYdBeWrdZBQYcQgRGXapJCKsZw8D",
            "D6eeDhanFgsQ4hrqDa6en2K3fcTmivg9SG",
            "D82zSUaviDPmCTy2QNkgSGKnDx2KkvYeCf",
            "DJZ6kntxHue1xk5Q1344XLDpjN6nefxJEF"
        )

        private val expectedHardenedAddresses = arrayOf(
            "DBWaJdzWzYG68hHuMZxRLXJpECo55aRBTY",
            "DNwJWZix5X4zkRm1RzEAGJwovYqBwo4j2c",
            "DBESbuLSvYYvnVw1jrMELA5SAt6aymxC1z",
            "DP1zDhs9miwpd2s6WHPKrjKyCWmhsQDFpD",
            "D6e8WyRxMm3d8rTDwESg7FwcXnfWgpi5uY",
            "DAieXxnkbff8oLFYuiHTF3kZUfjhC1ApbJ",
            "DANKK8qoCWxUeC6tz8DbmkH1FqfwkDPVqr",
            "DL8i5pQvUQbVGgoCpycQY2oxgD1LxthEAB",
            "DMwdDGeV1H2Kftp6cDJfnRCpevuwz4CWJ3",
            "DNu7iwy4FVij53yW77VSvkVr7S889FWgcC",
            "DD1NLyfxc4UeqfBZuDoEfLMh1o72SJ4bzM",
            "DM1ujCP6m9y2aQNW4uQJ91vAta59FoqRG7",
            "DRFgJeVFTzBg8TecmbRTKXkTPxwGS7baH8",
            "DJArvE1LKXtMk9NFL32qWvisfAx7N9whjc",
            "DHAbMEwoSk4Z1CpD11LYNzZ1pdH3ojeA27",
            "D6VFdLcFBDA6SodfDbPZr2V3vHnE4oE7ZX",
            "DBBopLwZ8sLBP2cqHFzbr2Y3vD1Qu2m1BL",
            "DT475uVwF52iKyUanmXjpuLFHsPmH5TwWq",
            "DNfusaU3YaNowHm4Jou6Si8Mr1hKHpm6qY",
            "DA4TMWPnAU25NuufKMxwNY1SnLCyWDvKMm",
            "DBgsr4kFkfTEopZn4cQPvnGkdgRih1HUB1",
            "DEHGVZwLNL2HpjoFoNzm24GFHrMWAWKsm6",
            "DSR61b5tn3oWvFhu3xSqEPTjjRQLxKH73o",
            "DRFbGigeburvQQt29Vqk21U47YcxGcACNA",
            "DHbHMkYcAYg9ucnMwLr6MmBMvCtCGMecu6",
            "DN8g7px5roXNb73kLgSMD3arbFAFgrojYM",
            "DLsdvbzFeo86CxMJVDPFDLX4zGSwuj2rGT",
            "DRyobWGtyD7ECLNYrFWoU1Acw5kA1FQLVQ",
            "DS2KWkntHXsoNaX7EykS23BtHmxwRWkJj1",
            "DA6SHEKCx8ARAZ1Ho4mdo73rPtKNHx94UN",
            "DRkzyRbpuwkUrN6y5pvJ8QVeoWj4hNrFFc",
            "DBQfk2jxiXY534GDyX7uc5vpaBQmkw6swr",
            "DJnxxoNUGeg3QqgJ8ZoFEwrCAHY92zxga3",
            "DFDcgPBSAVGW7HHmuZF4Z8bp9krx6naUjc",
            "D8w84YCMQdJnewHua3D6S8Ypem1UpHMx3A",
            "D7LgXKeGRyMhnxZe4Bqx6nf6h7SVA66yKL",
            "DTvYEyV2zECK9y4aCiT7UWWRQ7JuYAqspA",
            "DS32YicqJEHbTTw8Yy8PUvUDAYzrKRcNL6",
            "DU1EEFNzkUYoy4BYhqUX33ZjWM4yjxidYR",
            "DSeLFh8C5bn2VC2muMBL8qGMJpNBAvYfxz",
            "DJrS7bCNPh2HHnyxYjx9zszBQ6Dt3k6gZd",
            "DCrF5uPzGUqSgBqnfEe7MThBSAM4mBQNUf",
            "DR2HeRXTTiYT7rpWRWCuTcmX8DnNkGPRWb",
            "DRWTpvMBKkrrucgpY863DBQW7DjVfC87iw",
            "D7NpMxVMJJGGuEzrkS4X9SbPXuYNt66E4L",
            "DAFCjadBZP4FG5DbiV74NpEv2B8DsiC4DX",
            "DRmZrtoCFcUuhNsWng4acrswvkZDZLL4eC",
            "D7mCmH8TdsEuktHBrgFX7RkYWajCyDzNsB",
            "DEQwz5JabHGFfYasZwHVFtV9ssztu1Tbwu",
            "DSNSowyrt5nptHGEearodMLZ9nWJubGWp5",
            "DR1xcZpgNeb43WZpWEmK8cXFWYPkAHH5RA",
            "DQ1wMpnXX7Vdiirki5bqSi2cXHyUdwtXMZ",
            "DHxeR5DShjEBJUyXzXxHwRPKh4NYtHKRBf",
            "D5NVSHuKeL98QkdT46r18yJaw7A46toqH7",
            "D9QEzp4L9XAE2g5MzUfLD7Fjza2EgV13cj",
            "DGeRs1ky9gjSWUE2RVnqLQorZiNGfLzEmb",
            "DNUnEA4ER87ouBydHbyFboRmTwPhgwWfdr",
            "D9yWLCVTKCropdEaYoMRiS7ZQ1cDgLYAyC",
            "DBcETrFVCKdztKUQtRxdUoiY1y6BpdMtZF",
            "D64UQHsY9PmHgoWACEaK4Fwo7P83kR2Fix",
            "DL1vpVAgF5m53Hnxry2kyt8xV42Mmyg7Xh",
            "DL2PdrvpKihD1tRnb6aJWRfja2z7iK7iMC",
            "DP4QcbbM4bWmf8565ChMzftvXJ8ztsTwro",
            "DKnA1dfLyqt3zLn8MSUqdm1ikKPdU1anMA",
            "DAYKJgY6zPAvyQ93gUTuUFfdY3SeDjXmpQ",
            "DMy1HWCubTgr3FZEa2nn1bqH1SsFE7npNK",
            "DP2GW1ikhFA4DhHU4oXejUFSAicAz6jU8c",
            "DHM6BFE7xQHv4wonpPpo8Nx6BuakS6Uhw8",
            "D6mJfrYbgSaxh2uRXZEpXwXuFrNwvbrBmg",
            "D7XPDHTmZqpUB66359MdN7dkcxY7VKLdwF",
            "DJN5iVbLrB7njzns6zBU3o873adcrgFa7D",
            "D9bfP9rKCmNYxR3LZYT21CcE9UyWvDTNMi",
            "DF3SkoddDWShwjrUgZckkkzMJTZdfSytES",
            "DQLC4ZMqcsP8jSRgqwro4C5dfAyXcVs44h",
            "D9bXWTVW8bcTfPdo2LA1VrbtHG1vyhjv6Q",
            "D5zKqekox7bRf3Y6wpiHody6VwNFvffr55",
            "D7gmi9qAuyipniVjJYViTVUtVgP4u8sEid",
            "DN5M5w6iwc5qXAa1M52BhPit6BaUU9vWij",
            "DKCQ6zbK2zUHyq5HBGSTCyYifuN1MEYoVn",
            "DKeAPcgt6cnf7iC7aKxrbZPKoiuPf7Rm7x",
            "DFwPjKMeMzcx5Ctgc1E425N2mw9n2K4HVj",
            "D5x25PSfQFM6cQhHoZmH32D2RMt9dq4GS2",
            "D5toe55gDg3uFbKppCShY76qtCMtvncwcz",
            "DNKkpze8rMLaDaC2dV2rv4ASPZAiAfEGYg",
            "DLiDhm2Vdv9LBQn1JPdy7g6YoLtwchpf3F",
            "D7EiEmJTHtnWNsLqEVwoea9x9UzbV7fdd1",
            "DBjmUV5U3fvTqhC3VmKm31fyWbK8Bv24nk",
            "DEecYdJsqfBAnZU7fTaDpJddvA4SkUyFFh",
            "DQFDxvpuXMYnmiBgYNKYSayb8q9qhW1YSp",
            "DEci5HTjgkrd328oES1guJeW2Q5sdEMH91",
            "D965h79vPu4PDEtWHtU912NUvXquywgkew",
            "DDmaNGLNzPB1mYgcu4xaubpAKig5wp2Dip",
            "DNLG8611Ba2914TDoeayyGQTqM1ezLnH2o",
            "DEEdPrcjs9wQr58HGfLVV8vpEGkfoEkfP3",
            "DAZAiRV4AQFwwDQeH857wSYfNQzhWyVhPB",
            "DFoFh9oxY3QjyYgdmCpFaVJxSSZhNLU8zn",
            "DTTvXzEK53QoeBcMV6HCVA2dTJiaHn4Eb1",
            "DEvrBpJwWysjfsecpRWQUsYbcTCefxueBw",
            "DQAudcha7W5rwPm3Ax92nC36cpCr76PZSw",
            "DNkkEKjN52D3ak2RLsPg8uU5YbqTvrVzD8",
            "DSh9haKKFxWFSJgciamkyJoMqUzp1nNbDK"
        )
    }

}