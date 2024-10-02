package io.noone.androidcore.btclike.transaction


import io.noone.androidcore.types.UInt

object SigHashType {

    val ALL: UInt = UInt.of(1)
    val BCH_ALL: UInt = UInt.of(0x41)
}
