package io.noone.adnroidcore.btclike

import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.addresses.BitcoinCashAddress
import io.noone.androidcore.btclike.addresses.LegacyAddress
import io.noone.androidcore.btclike.networks.BchParams
import io.noone.androidcore.btclike.networks.BitcoinParams
import io.noone.androidcore.btclike.networks.DogeParams
import io.noone.androidcore.btclike.networks.LitecoinParams
import org.junit.Assert
import org.junit.Test

class WifTest {

    // for mnemonic: bunker ring viable sphere trap flush cost motor mixture transfer copy motor resist prize speed

    private val btcWifKeys = listOf(
        "L2djKztH4YYotB8co59cyC8oY5cn8NE2TBS2HewxbD6mbYZ8CnYs" to "1Ff7U7nbofhZzENXdHG6YJT7ngLUnPeSu6",
        "L3p2gv4xFh4cJfyvdSJxCEjhRz3sR1kzPZMQ2suSzHakXFQXnK72" to "1Hh224cwL9TSykhw1TQ6NrYz8nb3HXKwDa",
        "Ky7JPg9ZTHrmfK9M9mLbiBVr1Qc9eqYFuMzzRPLVRUy6kZee8JgZ" to "1E695t3y2mdj8qgSgirTsrMZRQ5rvURUPR",
        "L47Zq63VybeAoZQy2zYcMe57CVMAB41uKcAVYnnEhN5yiM79oDuy" to "1G379vXFeZTxNX1bkbsuomx7UxZT1vaKtU",
        "KzexmuPYQeE7d7rvLrdr8yi5ocYLXZBCq8JkfXJtAinGheqSoxBF" to "1MCmRquFKzbqEocwnbHhAJrnh85tZz5GCg",
    )

    private val ltcWifKeys = listOf(
        "T9wNQKWTJx4s5chwKwCyUYJVtcDRJCeenj7M49vxJYgwXEMkox8S" to "La9MhhHbKsRi8YCDVsNzm3huPvwoidbEGb",
        "T7cY2aCEP3ECUpz5fv1NEpNrJ5yrzLCiy5Wi1JqG9KuArJazABmT" to "LV2SYbNkydtXxckD6UJYMUCmM39o4gzTEj",
        "T99xWcETV6vjWY6p6MAtyK3zwcwGYaLdHSseyrmqjXN12Qd25Mds" to "Lh8uskqkLG8dfhLbSDrZkvfrBcBYQH9Wid",
        "TAKweQpasMK4QguethVF6WwPVs9tdCqWPkREPKw3qFHaVWbtqsES" to "LgJz2tRRMNUMSNoZdVpMAf3RjCA5xqAoST",
        "T645ufhfVHZc1EidwuGsxVGe5EXfcLnS5sHcKnzxvDmSBNCR4Hws" to "LYghutPTEvS2X9ELmw1131BfdGLEXEvkb7",
    )

    private val dogeWifKeys = listOf(
        "QPPfPfcTBqVz6ZRY4DmPu27Cj865tqSPz4TMEFNyWJ3iQpZngDCX" to "DFNWYyRv6w8ZGerbt7yKmqcr9DKFzyn57s",
        "QRtBRaur6vLmxp4jPXJN86jw5J9dECjjajQhvoACyQ5kaau9bGVM" to "D8Msibdav6W6ZyS6LphRDpov4vg6PwKehx",
        "QVVqgUNn6tMY2qsaFTfc8gxcKKDExE7qD1YwbqhzspUwHr1bWLTF" to "DKtV6dMjF8Ta1a66L1Zoxj15tUsAmj5Nuz",
        "QRhxEt88pk3adLKEfBzxfX1aUe4ZQ3CcYcYzD1mF11JTv8MBGvpW" to "DUQnsc2UMC6bQBR5bRzQJ1naLYspt2CoH4",
        "QQnNbHBXm2bShvASHr7FkJKdTRHhUPdKkCbvpuWsbFLdEL9ZYxXX" to "DC1LDGAEGCh72zscSevwgZ8fPhuUDoy1Sb",
    )

    private val bchWifKeys = listOf(
        "L4ZvxKpyRcCxBXfNm7VGPowKjM9N5kNZniunuNfWeCcePzi3e82L" to "qqgr780jzqqnq2ka5g823spxp7k24dptmux77sznat",
        "L2zENewWgSLgJBQCxsiozvTxxSnXscegaL3NUKHWwoMSbqGWnp6m" to "qr9ll8nzx5w9srg40vzqdp2yuu30lngsvypccnul9a",
        "Kxk37naHWfzFgx1PzYEedZ1AQjsyjF383Qao1JJRebsBj4XTAaVZ" to "qqc5w8swckq8kfrudyzwxmmtf5nf9hmqju4t2uth3y",
        "L1KRdqnVQXkNiKdLxRe7Y4AzzsWdh4oSDDLWknZGFpCPqxRdBfVf" to "qrf29kx6zxhpp5zw957zvs60752eva9k3cmll33vc5",
        "L4gmHy7QjZD3EvbN2VJ7A7y9sJn9M6S8J9kWbqbz1fM3paRb79MX" to "qrx0l6kk6y0l2tv2kw78lgjvcnyjdsn7cshuhlq32k",
    )


    @Test
    fun testWifDecoder() {
        for ((key, _) in btcWifKeys) {
            Assert.assertThrows(IllegalArgumentException::class.java) {
                PrivateKey.fromWif(key, DogeParams)
            }
            Assert.assertThrows(IllegalArgumentException::class.java) {
                PrivateKey.fromWif(key, LitecoinParams)
            }
        }

        for ((key, expectedAddress) in btcWifKeys) {
            val privateKey = PrivateKey.fromWif(key, BitcoinParams)
            val address = LegacyAddress.fromKey(BitcoinParams, privateKey.key)
            Assert.assertEquals(expectedAddress, address.toString())
        }
        for ((key, expectedAddress) in ltcWifKeys) {
            val privateKey = PrivateKey.fromWif(key, LitecoinParams)
            val address = LegacyAddress.fromKey(LitecoinParams, privateKey.key)
            Assert.assertEquals(expectedAddress, address.toString())
        }
        for ((key, expectedAddress) in bchWifKeys) {
            val privateKey = PrivateKey.fromWif(key, BchParams)
            val address = BitcoinCashAddress.fromKey(BchParams, privateKey.key)
            Assert.assertEquals(expectedAddress, address.bech32)
        }
        for ((key, expectedAddress) in dogeWifKeys) {
            val privateKey = PrivateKey.fromWif(key, DogeParams)
            val address = LegacyAddress.fromKey(DogeParams, privateKey.key)
            Assert.assertEquals(expectedAddress, address.toString())
        }
    }

}