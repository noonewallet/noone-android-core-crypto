package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.PrivateKey

class UnspentOutput(
    val txHash: String,
    var txOutputN: Int,
    val script: String,
    var value: Long,
    val privateKey: PrivateKey
)