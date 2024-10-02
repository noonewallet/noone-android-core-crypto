package io.noone.adnroidcore.btclike

internal fun Any.print() = println(toString())

internal fun <T> List<T>.print() {
    this.joinToString(
        prefix = "\"",
        postfix = "\"",
        separator = "\", \n\""
    ) { it.toString() }
        .print()
}
