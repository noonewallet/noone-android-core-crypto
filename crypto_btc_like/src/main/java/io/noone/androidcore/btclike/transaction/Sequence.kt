package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.types.UInt

data class Sequence(
    val value: UInt
) {

    companion object {
        val MAX = Sequence(UInt.of(-1))
        val ZERO = Sequence(UInt.of(0))
    }
}